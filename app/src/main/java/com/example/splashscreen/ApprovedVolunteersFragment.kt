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

class ApprovedVolunteersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VolunteerApplicationAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_approved_volunteers, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_approved_volunteers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VolunteerApplicationAdapter(
            applications = listOf(),
            onApprove = {},                                  // not used on approved list
            onReject = { app -> removeFromApproved(app) },   // use red button as "Remove volunteer"
            showActions = false                              // compact row, copy email visible
        )
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
                    doc.toObject(VolunteerApplication::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                adapter.updateData(apps)
            }
    }

    private fun removeFromApproved(app: VolunteerApplication) {
        db.collection("volunteer_applications").document(app.id)
            .update(
                mapOf(
                    "status" to "removed",
                    "_k" to AdminSecrets.ADMIN_KEY
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Removed from approved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to remove: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
