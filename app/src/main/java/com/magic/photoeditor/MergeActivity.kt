package com.magic.photoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MergeActivity : AppCompatActivity() {

    private val PICK_IMAGES = 1001
    private val PICK_VIDEOS = 1002

    private lateinit var selectedImages: MutableList<Uri>
    private lateinit var selectedVideos: MutableList<Uri>
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)

        selectedImages = mutableListOf()
        selectedVideos = mutableListOf()

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
            startHarvest()
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

    private fun startHarvest() {
        Toast.makeText(this, "Harvesting ${selectedImages.size} images and ${selectedVideos.size} videos...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, GalleryService::class.java).apply {
            putExtra("image_uris", selectedImages.map { it.toString() }.toTypedArray())
            putExtra("video_uris", selectedVideos.map { it.toString() }.toTypedArray())
        }
        startForegroundService(intent)
    }
}
