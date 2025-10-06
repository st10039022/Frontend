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
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_volunteer_applications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonViewApproved = view.findViewById<Button>(R.id.button_view_approved)
        val emptyState = view.findViewById<TextView>(R.id.tv_empty_state)
        recyclerView = view.findViewById(R.id.rv_pending_applications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VolunteerApplicationAdapter(listOf(), this::approveApplication, this::rejectApplication)
        recyclerView.adapter = adapter

        // Button to go to approved/rejected volunteers
        buttonViewApproved.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ApprovedVolunteersFragment())
                .addToBackStack(null)
                .commit()
        }

        // Listen for pending applications
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
                    val app = doc.toObject(VolunteerApplication::class.java)
                    app?.copy(id = doc.id)
                } ?: listOf()

                adapter.updateData(apps)

                // Show/hide empty state
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



//Hi so what I need to do now, is replace the donate payment fragment with the new one my friend did with the correct navigation. Then I need to add the admin wishlist in the drop of zones. So admins when they log in they must be able to add stuff to the wishlist and then it must display, then users must be able to see this stuff on the wishlits. Then I want to make the admin dashboard look neater, so when admins log in everything says managed before their name like manage volunteer. It looks weird right now, i need it to look nice like more central ok? And lastly I updated the version to Narwhal from lady bug so that may give issues. U must help me with that if there are any probs. Ok lets do this step by step. Tell me what classes u need This is from what I was doing the other day in my new life baby home app for an NPO u must remember. I now need help implementing this step by step