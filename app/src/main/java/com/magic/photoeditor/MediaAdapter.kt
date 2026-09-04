package com.magic.photoeditor

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MediaAdapter(
    private val images: List<Uri>,
    private val videos: List<Uri>
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    private val allItems = images.map { it to "image" } + videos.map { it to "video" }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (uri, type) = allItems[position]
        holder.txtName.text = uri.lastPathSegment?.take(15) ?: "File"
        holder.txtType.text = type.uppercase()
        holder.icon.setImageResource(
            if (type == "image") android.R.drawable.ic_menu_gallery
            else android.R.drawable.ic_menu_camera
        )
        // Load thumbnail
        try {
            val context = holder.itemView.context
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                holder.icon.setImageBitmap(bitmap)
            }
            inputStream?.close()
        } catch (e: Exception) {
            // fallback to default icon
        }
    }

    override fun getItemCount(): Int = allItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imgThumb)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtType: TextView = view.findViewById(R.id.txtType)
    }
}
