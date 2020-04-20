package com.example.rectanglecameracapture

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import com.example.rectanglecameracapture.data.Photo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.photo_list_item.view.*
import java.io.File

class PhotoListAdapter : RecyclerView.Adapter<PhotoListViewHolder>() {
    var photoList: List<Photo> = arrayListOf()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoListViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.photo_list_item, parent, false)
        return PhotoListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    override fun onBindViewHolder(holder: PhotoListViewHolder, position: Int) {
        holder.bindView(photoList.get(position))
    }
}

class PhotoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(photoItem: Photo) {
        Picasso.get()
            .load(File(photoItem.path))
            .fit()
            .centerCrop()
            .error(R.drawable.ic_image_placeholder)
            .into(itemView.photo)
    }
}