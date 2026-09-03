package com.magic.photoeditor

import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import java.net.NetworkInterface
import java.net.InetAddress
import java.util.*

object Utils {
    fun getDeviceInfo(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = if (android.os.Build.VERSION.SDK_INT >= 26) {
            tm.imei ?: "N/A"
        } else {
            tm.deviceId ?: "N/A"
        }
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val ip = getPublicIp()
        return """
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android: ${android.os.Build.VERSION.RELEASE}
            IMEI: $imei
            Android ID: $androidId
            IP: $ip
        """.trimIndent()
    }

    private fun getPublicIp(): String {
        return try {
            val url = java.net.URL("https://api.ipify.org")
            val conn = url.openConnection()
            conn.connect()
            conn.getInputStream().bufferedReader().readText()
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
