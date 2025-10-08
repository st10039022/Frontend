package com.example.splashscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestimonialsAdapter (
    private val items: MutableList<Testimonial>,
    private val isAdmin: Boolean = false,
    private val onEdit: ((Testimonial) -> Unit)? = null,
    private val onDelete: ((Testimonial) -> Unit)? = null,
    private val onImageRemove: ((testimonial: Testimonial, imageUrl: String) -> Unit)? = null
) : RecyclerView.Adapter<TestimonialsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvImages: RecyclerView = itemView.findViewById(R.id.recyclerImages)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val adminButtons: View = itemView.findViewById(R.id.adminButtons)
        val btnEdit: ImageButton? = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton? = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_testimonial, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.tvName.text = t.familyName
        holder.tvMessage.text = t.message
        val createdDate = t.createdAt.toDate() // Timestamp -> Date
        holder.tvDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(createdDate)

        // images horizontal
        holder.rvImages.layoutManager =
            LinearLayoutManager(holder.itemView.context, RecyclerView.HORIZONTAL, false)
        val imagesAdapter = ImagesAdapter(t.images, isAdmin,
            onImageClick = { /* maybe show full screen */ },
            onRemoveClick = { url ->
                onImageRemove?.invoke(t, url)
            })
        holder.rvImages.adapter = imagesAdapter

        // admin controls
        holder.adminButtons.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.btnEdit?.setOnClickListener { onEdit?.invoke(t) }
        holder.btnDelete?.setOnClickListener { onDelete?.invoke(t) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Testimonial>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}