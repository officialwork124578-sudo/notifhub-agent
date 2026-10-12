package com.onxy.notifhub

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("notifhub", MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val title = TextView(this).apply {
            text = "NotifHub Agent"
            textSize = 22f
        }
        val sub = TextView(this).apply {
            text = "Connect this device to NotifHub server."
            textSize = 13f
            setPadding(0, 16, 0, 32)
        }

        val urlLabel = TextView(this).apply { text = "Server WS URL"; textSize = 12f }
        val urlInput = EditText(this).apply {
            setText(prefs.getString("server", "ws://100.77.29.105:3000/ws/ingest"))
            hint = "ws://host:3000/ws/ingest"
        }

        val tokenLabel = TextView(this).apply { text = "Device token (optional)"; textSize = 12f; setPadding(0,24,0,0) }
        val tokenInput = EditText(this).apply {
            setText(prefs.getString("token", ""))
            hint = "secret"
        }

        val saveBtn = Button(this).apply {
            text = "Save & Start Agent"
        }

        val notifBtn = Button(this).apply {
            text = "Enable Notification Access"
        }

        val status = TextView(this).apply {
            textSize = 12f
            setPadding(0, 24, 0, 0)
        }

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        status.text = "Device ID: ${deviceId.take(12)}…\nModel: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}"

        saveBtn.setOnClickListener {
            prefs.edit()
                .putString("server", urlInput.text.toString().trim())
                .putString("token", tokenInput.text.toString().trim())
                .apply()
            val i = Intent(this, AgentService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            Toast.makeText(this, "Agent started", Toast.LENGTH_SHORT).show()
        }

        notifBtn.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        layout.addView(title)
        layout.addView(sub)
        layout.addView(urlLabel); layout.addView(urlInput)
        layout.addView(tokenLabel); layout.addView(tokenInput)
        layout.addView(saveBtn)
        layout.addView(notifBtn)
        layout.addView(status)

        setContentView(layout)
    }
}
