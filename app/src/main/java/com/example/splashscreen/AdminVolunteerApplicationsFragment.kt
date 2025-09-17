package com.example.splashscreen


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
import com.example.splashscreen.VolunteerApplication

class AdminVolunteerApplicationsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VolunteerApplicationAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_volunteer_applications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv_pending_applications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VolunteerApplicationAdapter(listOf(), this::approveApplication, this::rejectApplication)
        recyclerView.adapter = adapter
        listenForPendingApplications()
    }

    private fun listenForPendingApplications() {
        listener = db.collection("volunteer_applications")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading applications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val apps = snapshots?.documents?.mapNotNull { doc ->
                    val app = doc.toObject(VolunteerApplication::class.java)
                    app?.copy(id = doc.id)
                } ?: listOf()
                adapter.updateData(apps)
            }
    }

    private fun approveApplication(app: VolunteerApplication) {
        db.collection("volunteer_applications").document(app.id)
            .update("status", "approved")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application approved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to approve", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectApplication(app: VolunteerApplication) {
        db.collection("volunteer_applications").document(app.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application rejected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to reject", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}