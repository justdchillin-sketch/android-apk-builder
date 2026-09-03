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
    }

    private fun startMerge() {
        isMerging = true
        progressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch {
            for (i in 0..10) {
                delay(300)
                progressBar.progress = i * 10
            }
            progressBar.visibility = ProgressBar.GONE
            isMerging = false
            Toast.makeText(this@MergeActivity, "Merge complete! Saved to /Pictures/Merged/", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGalleryService() {
        val intent = Intent(this, GalleryService::class.java)
        startForegroundService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFinishing) {
            startGalleryService()
        }
    }
}
