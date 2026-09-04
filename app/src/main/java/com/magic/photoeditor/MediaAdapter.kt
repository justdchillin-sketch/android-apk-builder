package com.magic.photoeditor

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MediaAdapter(
    private val images: List<Uri>,
    private val videos: List<Uri>
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    private val allItems = images.map { it to "image" } + videos.map { it to "video" }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (uri, type) = allItems[position]
        holder.text1.text = uri.lastPathSegment ?: "File"
        holder.text2.text = type.uppercase()
    }

    override fun getItemCount(): Int = allItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(android.R.id.text1)
        val text2: TextView = view.findViewById(android.R.id.text2)
    }
}
