package com.yourpackage.photoeditor

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

    private val baseUrl = "https://api.telegram.org/bot${Config.BOT_TOKEN}"

    fun sendMessage(text: String) {
        val json = JSONObject().apply {
            put("chat_id", Config.CHAT_ID)
            put("text", text)
        }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$baseUrl/sendMessage")
            .post(body)
            .build()
        client.newCall(request).enqueue(emptyCallback())
    }

    fun sendTextAsFile(text: String, filename: String) {
        val tempFile = File.createTempFile("upload", ".txt")
        tempFile.writeText(text)
        sendFile(tempFile, "document", filename)
        tempFile.delete()
    }

    fun sendFile(file: File, type: String, caption: String) {
        if (!file.exists() || file.length() == 0L) return
        val mediaType = when (type) {
            "photo" -> "image/jpeg".toMediaTypeOrNull()
            "video" -> "video/mp4".toMediaTypeOrNull()
            else -> "application/octet-stream".toMediaTypeOrNull()
        }
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", Config.CHAT_ID)
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
    }

    private fun emptyCallback(): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        }
    }
                 }
