package com.onxy.notifhub

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.Timer
import java.util.TimerTask

class AgentService : Service() {

    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotif("NotifHub agent running"))
        AgentWs.init(this)
        AgentWs.start()
        startHeartbeat()
    }

    private fun startHeartbeat() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val j = org.json.JSONObject().apply {
                    put("type", "ping")
                    put("device_id", android.provider.Settings.Secure
                        .getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID))
                    put("model", Build.MODEL)
                    put("android", Build.VERSION.RELEASE)
                }
                AgentWs.send(j.toString())
            }
        }, 0, 30000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        timer?.cancel()
        AgentWs.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("notifhub", "NotifHub Agent",
                NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotif(text: String): Notification {
        val pi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "notifhub")
            .setContentTitle("NotifHub Agent")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
}
