package com.example.mahalapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mahalapp.models.MyFile
import com.example.mahalapp.R

// Adapter for displaying files in a RecyclerView
class FilesAdapter(
    private val items: List<MyFile>,                // List of files to show
    private val onDownloadClick: (MyFile) -> Unit   // Callback when download is clicked
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    // ViewHolder: Holds references to views in each file item layout
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val downloadIcon: ImageView = view.findViewById(R.id.downloadIcon)
    }

    // Inflate file item layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(v)
    }

    // Bind file data to the item view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = items[position]
        holder.nameText.text = file.name

        // Click on the whole card triggers download
        holder.view.setOnClickListener {
            onDownloadClick(file)
        }

        // Click on the download icon also triggers download
        holder.downloadIcon.setOnClickListener {
            onDownloadClick(file)
        }
    }

    // Number of items in the list
    override fun getItemCount() = items.size
}
