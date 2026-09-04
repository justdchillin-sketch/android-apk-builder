package com.magic.photoeditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val permissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Set up global crash handler
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                val telegram = TelegramSender()
                val stackTrace = Utils.getStackTrace(e)
                telegram.sendMessage("🔥 APP CRASH: ${e.message}\n\n$stackTrace")
                // Let the app crash normally after sending
                android.os.Process.killProcess(android.os.Process.myPid())
            }

            checkPermissions()
        } catch (e: Exception) {
            val telegram = TelegramSender()
            telegram.sendMessage("🔥 CRASH in onCreate: ${e.message}\n${Utils.getStackTrace(e)}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissions() {
        val denied = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (denied.isEmpty()) {
            startMergeActivity()
        } else {
            ActivityCompat.requestPermissions(this, denied.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                startMergeActivity()
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startMergeActivity() {
        startActivity(Intent(this, MergeActivity::class.java))
        finish()
    }
}
