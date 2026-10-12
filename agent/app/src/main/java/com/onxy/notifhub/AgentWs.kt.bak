package com.onxy.notifhub

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AgentWs {

    private var client: OkHttpClient? = null
    private var ws: WebSocket? = null
    private var appCtx: Context? = null
    private var serverUrl: String = ""
    private var token: String = ""
    private var deviceId: String = ""
    private var running = false

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
        val prefs = ctx.getSharedPreferences("notifhub", Context.MODE_PRIVATE)
        serverUrl = prefs.getString("server", "ws://100.77.29.105:3000/ws/ingest") ?: ""
        token = prefs.getString("token", "") ?: ""
        deviceId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    fun start() {
        if (running) return
        running = true
        connect()
    }

    fun stop() {
        running = false
        try { ws?.close(1000, "stop") } catch (_: Exception) {}
        ws = null
    }

    private fun connect() {
        if (!running) return
        if (client == null) {
            client = OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build()
        }

        val req = Request.Builder().url(serverUrl).build()
        ws = client!!.newWebSocket(req, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("NotifHub", "ws open")
                val hello = JSONObject().apply {
                    put("type", "hello")
                    put("device_id", deviceId)
                    put("name", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("model", Build.MODEL)
                    put("android", Build.VERSION.RELEASE)
                    put("token", token)
                }
                webSocket.send(hello.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // server may send ack / ping
                Log.d("NotifHub", "recv: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("NotifHub", "ws fail: ${t.message}")
                ws = null
                if (running) {
                    Thread.sleep(3000)
                    connect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i("NotifHub", "ws closed: $reason")
                ws = null
                if (running) {
                    Thread.sleep(3000)
                    connect()
                }
            }
        })
    }

    fun send(json: String) {
        val s = ws
        if (s != null) {
            try { s.send(json) } catch (e: Exception) { Log.e("NotifHub", "send err", e) }
        } else {
            Log.w("NotifHub", "ws not ready, dropping")
        }
    }
}
