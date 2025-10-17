package com.example.splashscreen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class VolunteerApplicationAdapter(
    private var applications: List<VolunteerApplication>,
    private val onApprove: (VolunteerApplication) -> Unit,
    private val onReject: (VolunteerApplication) -> Unit,   // used as "Remove" in approved screen
    private val showActions: Boolean = true                  // true = pending screen, false = approved screen
) : RecyclerView.Adapter<VolunteerApplicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tv_name)
        val emailText: TextView = view.findViewById(R.id.tv_email)
        val availabilityText: TextView = view.findViewById(R.id.tv_availability)
        val whyText: TextView = view.findViewById(R.id.tv_why)
        val experienceText: TextView = view.findViewById(R.id.tv_experience)
        val approveButton: Button = view.findViewById(R.id.btn_approve)
        val rejectButton: Button = view.findViewById(R.id.btn_reject)
        val copyEmailButton: Button = view.findViewById(R.id.btn_copy_email) // in your item XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_volunteer_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        val ctx = holder.itemView.context

        holder.nameText.text = app.name
        holder.emailText.text = app.email

        if (showActions) {
            // -------------------------
            // PENDING LIST
            // -------------------------
            holder.copyEmailButton.visibility = View.GONE
            holder.copyEmailButton.setOnClickListener(null)

            // restore big split buttons (weight = 1 each)
            (holder.approveButton.layoutParams as LinearLayout.LayoutParams).apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                weight = 1f
                marginEnd = 0
            }
            holder.approveButton.layoutParams = holder.approveButton.layoutParams
            holder.approveButton.text = "Approve"
            holder.approveButton.minWidth = 0
            holder.approveButton.setPadding(0, 0, 0, 0)
            holder.approveButton.setTextColor(Color.WHITE)
            holder.approveButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
            holder.approveButton.visibility = View.VISIBLE
            holder.approveButton.setOnClickListener { onApprove(app) }

            holder.rejectButton.visibility = View.VISIBLE
            holder.rejectButton.text = "Reject"
            (holder.rejectButton.layoutParams as LinearLayout.LayoutParams).apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                weight = 1f
                // keep your left margin from XML
            }
            holder.rejectButton.layoutParams = holder.rejectButton.layoutParams
            holder.rejectButton.setOnClickListener { onReject(app) }

            // plain values (no labels) to match your original pending UI
            holder.availabilityText.text = app.availability
            holder.whyText.text = app.why
            holder.experienceText.text = app.experience
        } else {
            // -------------------------
            // APPROVED LIST
            // -------------------------
            // show Copy email
            holder.copyEmailButton.visibility = View.VISIBLE
            holder.copyEmailButton.text = "Copy email"
            holder.copyEmailButton.setOnClickListener {
                val email = app.email.trim()
                if (email.isEmpty()) {
                    Toast.makeText(ctx, "No email to copy", Toast.LENGTH_SHORT).show()
                } else {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Volunteer email", email))
                    Toast.makeText(ctx, "Email copied", Toast.LENGTH_SHORT).show()
                }
            }

            // hide the big red Reject
            holder.rejectButton.visibility = View.GONE
            holder.rejectButton.setOnClickListener(null)

            // turn the APPROVE button into a SMALL "Remove volunteer"
            holder.approveButton.visibility = View.VISIBLE
            holder.approveButton.text = "Remove volunteer"
            holder.approveButton.setTextColor(Color.WHITE)

            val smallLp = holder.approveButton.layoutParams as LinearLayout.LayoutParams
            smallLp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            smallLp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            smallLp.weight = 0f
            smallLp.marginEnd = dp(holder, 8)
            holder.approveButton.layoutParams = smallLp

            holder.approveButton.minWidth = 0
            holder.approveButton.setPadding(dp(holder, 12), dp(holder, 6), dp(holder, 12), dp(holder, 6))
            holder.approveButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.red))
            holder.approveButton.setOnClickListener { onReject(app) }

            // labeled fields to mirror the application
            holder.availabilityText.text = "Dates available: ${app.availability.ifBlank { "—" }}"
            holder.whyText.text = "Why they signed up: ${app.why.ifBlank { "—" }}"
            holder.experienceText.text = "Prior experience: ${app.experience.ifBlank { "—" }}"
        }
    }

    override fun getItemCount() = applications.size

    fun updateData(newApps: List<VolunteerApplication>) {
        applications = newApps
        notifyDataSetChanged()
    }

    private fun dp(holder: ViewHolder, value: Int): Int =
        (value * holder.itemView.resources.displayMetrics.density + 0.5f).toInt()
}
