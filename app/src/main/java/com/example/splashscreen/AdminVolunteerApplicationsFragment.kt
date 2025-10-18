package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminVolunteerApplicationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VolunteerApplicationAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_volunteer_applications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonViewApproved = view.findViewById<Button>(R.id.button_view_approved)
        val buttonViewRejected = view.findViewById<Button>(R.id.button_view_rejected)
        val emptyState = view.findViewById<TextView>(R.id.tv_empty_state)

        recyclerView = view.findViewById(R.id.rv_pending_applications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VolunteerApplicationAdapter(
            applications = listOf(),
            onApprove = { app -> approveApplication(app) },
            onReject = { app -> rejectApplication(app) },
            showActions = true // pending screen shows Approve/Reject
        )
        recyclerView.adapter = adapter

        buttonViewApproved.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ApprovedVolunteersFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonViewRejected.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RejectedVolunteersFragment())
                .addToBackStack(null)
                .commit()
        }

        listenForPendingApplications(emptyState)
    }

    private fun listenForPendingApplications(emptyState: TextView) {
        listener = db.collection("volunteer_applications")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading applications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val apps = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(VolunteerApplication::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                adapter.updateData(apps)

                if (apps.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }

    private fun approveApplication(app: VolunteerApplication) {
        db.collection("volunteer_applications").document(app.id)
            .update(
                mapOf(
                    "status" to "approved",
                    "_k" to AdminSecrets.ADMIN_KEY
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application approved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to approve: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectApplication(app: VolunteerApplication) {
        db.collection("volunteer_applications").document(app.id)
            .update(
                mapOf(
                    "status" to "rejected",
                    "_k" to AdminSecrets.ADMIN_KEY
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application rejected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to reject: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
