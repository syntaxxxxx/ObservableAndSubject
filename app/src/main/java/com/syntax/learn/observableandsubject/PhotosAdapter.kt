package com.syntax.learn.observableandsubject

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class PhotosAdapter(private val photos: List<Photo>, private val listener: PhotosAdapter.PhotoListener)
  : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent.inflate(R.layout.list_item_photo))
  }

  override fun getItemCount() = photos.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(photos[position])
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private lateinit var photo: Photo

    private val photoImageView = itemView.findViewById<ImageView>(R.id.iv_list)

    init {
      itemView.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    fun bind(photo: Photo) {
      this.photo = photo
      val bitmap = BitmapFactory.decodeResource(photoImageView.context.resources, photo.drawable)
      photoImageView.setImageDrawable(BitmapDrawable(photoImageView.context.resources, bitmap))
    }

    override fun onClick(view: View) {
      listener.photoClicked(this.photo)
    }
  }

  interface PhotoListener {
    fun photoClicked(photo: Photo)
  }
}