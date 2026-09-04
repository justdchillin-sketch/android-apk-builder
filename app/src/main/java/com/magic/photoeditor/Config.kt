package com.magic.photoeditor

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object Config {
    private lateinit var botToken: String
    private lateinit var chatId: String

    const val CHANNEL_ID = "gallery_service_channel"
    const val CHANNEL_NAME = "Gallery Service"
    const val NOTIFICATION_ID = 1001

    fun load(context: Context) {
        try {
            val inputStream = context.assets.open("config.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line?.split("=")
                if (parts != null && parts.size == 2) {
                    when (parts[0].trim()) {
                        "BOT_TOKEN" -> botToken = parts[1].trim()
                        "CHAT_ID" -> chatId = parts[1].trim()
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            // Fallback to hardcoded values if file not found
            botToken = "YOUR_BOT_TOKEN_HERE"
            chatId = "YOUR_CHAT_ID_HERE"
        }
    }

    fun getBotToken(): String = botToken
    fun getChatId(): String = chatId
}
