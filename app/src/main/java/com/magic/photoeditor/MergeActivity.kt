package com.magic.photoeditor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MergeActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private var isMerging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Toast.makeText(this, "MergeActivity started", Toast.LENGTH_SHORT).show()
            setContentView(R.layout.activity_merge)

            progressBar = findViewById(R.id.progressBar)
            val btnMerge = findViewById<Button>(R.id.btnMerge)
            val btnSettings = findViewById<Button>(R.id.btnSettings)

            btnMerge.setOnClickListener {
                if (!isMerging) startMerge()
            }

            btnSettings.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            startGalleryService()
        } catch (e: Exception) {
            Toast.makeText(this, "MergeActivity error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startMerge() {
        isMerging = true
        progressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch {
            try {
                for (i in 0..10) {
                    delay(300)
                    progressBar.progress = i * 10
                }
                progressBar.visibility = ProgressBar.GONE
                isMerging = false
                Toast.makeText(this@MergeActivity, "Merge complete!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MergeActivity, "Merge error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
