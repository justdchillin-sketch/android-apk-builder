package com.magic.photoeditor

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import java.net.URL

object Utils {
    fun getDeviceInfo(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val ip = getPublicIp()
        return """
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android: ${android.os.Build.VERSION.RELEASE}
            Android ID: $androidId
            IP: $ip
        """.trimIndent()
    }

    private fun getPublicIp(): String {
        return try {
            val url = URL("https://api.ipify.org")
            val conn = url.openConnection()
            conn.connect()
            conn.getInputStream().bufferedReader().readText()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (it.moveToFirst()) {
                    it.getString(columnIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
