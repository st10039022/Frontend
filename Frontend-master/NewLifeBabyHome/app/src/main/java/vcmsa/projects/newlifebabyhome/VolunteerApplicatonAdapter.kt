package vcmsa.projects.newlifebabyhome




import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.newlifebabyhome.VolunteerApplication

class VolunteerApplicationAdapter(
    private var applications: List<VolunteerApplication>,
    private val onApprove: (VolunteerApplication) -> Unit,
    private val onReject: (VolunteerApplication) -> Unit,
    private val showActions: Boolean = true
) : RecyclerView.Adapter<VolunteerApplicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tv_name)
        val emailText: TextView = view.findViewById(R.id.tv_email)
        val availabilityText: TextView = view.findViewById(R.id.tv_availability)
        val whyText: TextView = view.findViewById(R.id.tv_why)
        val experienceText: TextView = view.findViewById(R.id.tv_experience)
        val approveButton: Button = view.findViewById(R.id.btn_approve)
        val rejectButton: Button = view.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_volunteer_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        holder.nameText.text = app.name
        holder.emailText.text = app.email
        holder.availabilityText.text = app.availability
        holder.whyText.text = app.why
        holder.experienceText.text = app.experience
        if (showActions) {
            holder.approveButton.visibility = View.VISIBLE
            holder.rejectButton.visibility = View.VISIBLE
            holder.approveButton.setOnClickListener { onApprove(app) }
            holder.rejectButton.setOnClickListener { onReject(app) }
        } else {
            holder.approveButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
            holder.approveButton.setOnClickListener(null)
            holder.rejectButton.setOnClickListener(null)
        }
    }

    override fun getItemCount() = applications.size

    fun updateData(newApps: List<VolunteerApplication>) {
        applications = newApps
        notifyDataSetChanged()
    }
}