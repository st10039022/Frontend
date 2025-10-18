package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AdminTestimonialsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private val firestore = FirebaseFirestore.getInstance()
    private val testimonials = mutableListOf<Testimonial>()
    private lateinit var adapter: TestimonialsAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_testimonials, container, false)

        recycler = view.findViewById(R.id.recyclerAdminTestimonials)
        fabAdd = view.findViewById(R.id.fabAddTestimonial)

        fabAdd.visibility = if (SessionManager.isAdmin) View.VISIBLE else View.GONE
        if (SessionManager.isAdmin) fabAdd.setOnClickListener { showAddEditDialog(existing = null) }

        adapter = TestimonialsAdapter(
            items = testimonials,
            isAdmin = SessionManager.isAdmin,
            onEdit = { t -> showAddEditDialog(t) },
            onDelete = { t -> confirmAndDelete(t) }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        loadTestimonials()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.backButtonTestimonials).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadTestimonials() {
        listener = firestore.collection("testimonials")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error loading testimonials", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    val t = d.toObject(Testimonial::class.java) ?: Testimonial()
                    t.copy(id = d.id, createdAt = t.createdAt)
                } ?: emptyList()
                adapter.update(list)
            }
    }

    private fun showAddEditDialog(existing: Testimonial?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_testimonial, null)

        val etName = dialogView.findViewById<EditText>(R.id.etFamilyName)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessage)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Hide any legacy image controls if they exist in layout
        hideIfExists(dialogView, "btnPickImages")
        hideIfExists(dialogView, "tvPickedCount")
        hideIfExists(dialogView, "imagesPreviewContainer")

        if (existing != null) {
            etName.setText(existing.familyName)
            etMessage.setText(existing.message)
        }

        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val message = etMessage.text.toString().trim()
            if (name.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), "Name and message required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveTestimonial(existing, name, message) { alert.dismiss() }
        }

        alert.show()
    }

    private fun saveTestimonial(
        existing: Testimonial?,
        name: String,
        message: String,
        onDone: () -> Unit
    ) {
        val docRef = existing?.let { firestore.collection("testimonials").document(it.id) }
            ?: firestore.collection("testimonials").document()

        // Keep existing images as is
        val finalImages = existing?.images ?: emptyList()

        val data = mapOf(
            "familyName" to name,
            "message" to message,
            "images" to finalImages,
            "createdAt" to (existing?.createdAt ?: Timestamp.now())
        )

        docRef.set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                onDone()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(requireContext(), "Failed to save: ${ex.message}", Toast.LENGTH_LONG).show()
                onDone()
            }
    }

    private fun confirmAndDelete(t: Testimonial) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete testimonial")
            .setMessage("Delete this testimonial?")
            .setPositiveButton("Delete") { _, _ -> deleteTestimonial(t) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTestimonial(t: Testimonial) {
        firestore.collection("testimonials").document(t.id).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(requireContext(), "Failed to delete: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideIfExists(root: View, idName: String) {
        val id = root.resources.getIdentifier(idName, "id", root.context.packageName)
        if (id != 0) root.findViewById<View>(id)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
