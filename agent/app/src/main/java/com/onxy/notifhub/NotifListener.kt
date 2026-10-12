package com.onxy.notifhub

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class NotifListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val pkg = sbn.packageName ?: return
            // skip our own
            if (pkg == packageName) return

            val n = sbn.notification ?: return
            val ex: Bundle = n.extras ?: Bundle()

            val title  = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text   = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val big    = ex.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val sub    = ex.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
            val cat    = n.category ?: ""
            val chan   = if (android.os.Build.VERSION.SDK_INT >= 26) n.channelId ?: "" else ""

            // lines (InboxStyle)
            val linesArr = JSONArray()
            val lines = ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            if (lines != null) for (l in lines) linesArr.put(l?.toString() ?: "")

            // sender (MessagingStyle)
            var sender = ""
            val msgs = ex.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (msgs != null && msgs.isNotEmpty()) {
                val last = msgs.last() as? Bundle
                sender = last?.getCharSequence("sender")?.toString() ?: ""
                val mtxt = last?.getCharSequence("text")?.toString()
                if (!mtxt.isNullOrEmpty() && text.isEmpty()) {
                    // use as text
                    val j = JSONObject()
                    j.put("type", "notif")
                    j.put("pkg", pkg)
                    j.put("app_label", appLabel(pkg))
                    j.put("category", cat)
                    j.put("channel", chan)
                    j.put("title", title)
                    j.put("text", mtxt)
                    j.put("big_text", big)
                    j.put("sender", sender)
                    j.put("thread_id", sbn.key ?: "")
                    j.put("posted_at", sbn.postTime)
                    j.put("lines", linesArr)
                    AgentWs.send(j.toString())
                    return
                }
            }

            val j = JSONObject()
            j.put("type", "notif")
            j.put("pkg", pkg)
            j.put("app_label", appLabel(pkg))
            j.put("category", cat)
            j.put("channel", chan)
            j.put("title", title)
            j.put("text", text)
            j.put("big_text", big)
            j.put("sub_text", sub)
            j.put("sender", sender)
            j.put("thread_id", sbn.key ?: "")
            j.put("posted_at", sbn.postTime)
            j.put("lines", linesArr)

            AgentWs.send(j.toString())
        } catch (e: Exception) {
            Log.e("NotifHub", "post err", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // optional: track removal
    }

    private fun appLabel(pkg: String): String {
        return try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) { pkg }
    }
}
