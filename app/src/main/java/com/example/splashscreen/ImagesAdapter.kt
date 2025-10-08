package com.example.splashscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImagesAdapter(
    private val images: List<String>,             // Full download URLs
    private val isAdmin: Boolean = false,
    private val onImageClick: ((String) -> Unit)? = null,
    private val onRemoveClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<ImagesAdapter.ImageVH>() {

    inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.ivImage)
        val btnRemove: ImageButton? = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_testimonial_image, parent, false)
        return ImageVH(v)
    }

    override fun onBindViewHolder(holder: ImageVH, position: Int) {
        val url = images[position] // Already a full Firebase Storage download URL

        // Load directly with Glide
        Glide.with(holder.itemView.context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_placeholder_image) // optional placeholder
            .error(R.drawable.ic_placeholder_image)       // fallback if URL fails
            .into(holder.iv)

        holder.itemView.setOnClickListener { onImageClick?.invoke(url) }
        holder.btnRemove?.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.btnRemove?.setOnClickListener { onRemoveClick?.invoke(url) }
    }

    override fun getItemCount(): Int = images.size
}