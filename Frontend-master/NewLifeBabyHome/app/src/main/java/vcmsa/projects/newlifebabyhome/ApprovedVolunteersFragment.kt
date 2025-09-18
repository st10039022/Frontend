package vcmsa.projects.newlifebabyhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import vcmsa.projects.newlifebabyhome.VolunteerApplication

class ApprovedVolunteersFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VolunteerApplicationAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approved_volunteers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv_approved_volunteers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Hide approve/reject actions in approved list
        adapter = VolunteerApplicationAdapter(listOf(), {}, {}, showActions = false)
        recyclerView.adapter = adapter
        listenForApprovedVolunteers()
    }

    private fun listenForApprovedVolunteers() {
        listener = db.collection("volunteer_applications")
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading volunteers", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val apps = snapshots?.documents?.mapNotNull { doc ->
                    val app = doc.toObject(VolunteerApplication::class.java)
                    app?.copy(id = doc.id)
                } ?: listOf()
                adapter.updateData(apps)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}