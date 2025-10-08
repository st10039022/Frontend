package com.example.splashscreen


import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.jvm.java
import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.replace
import kotlin.text.trim
import kotlin.to


class AdminTestimonialsFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val testimonials = mutableListOf<Testimonial>()
    private lateinit var adapter: TestimonialsAdapter
    private var listener: ListenerRegistration? = null

    // Temp storage for images picked via ActivityResult
    private var tempPickedUris: List<Uri> = emptyList()
    private var dialogTvPickedCount: TextView? = null

    // Register image picker
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            tempPickedUris = uris
            dialogTvPickedCount?.text = "${uris.size} image(s) selected"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_testimonials, container, false)

        recycler = view.findViewById(R.id.recyclerAdminTestimonials)
        fabAdd = view.findViewById(R.id.fabAddTestimonial)

        // Show FAB only if the logged-in user is admin
        fabAdd.visibility = if (SessionManager.isAdmin) View.VISIBLE else View.GONE
        if (SessionManager.isAdmin) {
            fabAdd.setOnClickListener {
                showAddEditDialog(null)
            }
        }

        adapter = TestimonialsAdapter(
            items = testimonials,
            isAdmin = SessionManager.isAdmin, // pass admin status to adapter
            onEdit = { t -> showAddEditDialog(t) },
            onDelete = { t -> confirmAndDelete(t) },
            onImageRemove = { t, url -> deleteImageFromTestimonial(t, url) }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        loadTestimonials()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
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
                    // Convert Timestamp to millis if you need it anywhere
                    val createdMillis = t.createdAt.toDate().time
                    t.copy(id = d.id, createdAt = t.createdAt) // keep Timestamp in data class
                } ?: emptyList()

                adapter.update(list)
            }
    }

    private fun showAddEditDialog(existing: Testimonial?) {
        // Make sure the dialog layout file exists: res/layout/dialog_add_edit_testimonial.xml
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_testimonial, null)
        val etName = dialogView.findViewById<EditText>(R.id.etFamilyName)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessage)
        val btnPick = dialogView.findViewById<Button>(R.id.btnPickImages)
        val tvCount = dialogView.findViewById<TextView>(R.id.tvPickedCount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        dialogTvPickedCount = tvCount
        tempPickedUris = emptyList()
        tvCount.text = "No images chosen"

        if (existing != null) {
            etName.setText(existing.familyName)
            etMessage.setText(existing.message)
            tvCount.text = "${existing.images.size} existing image(s)"
        }

        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnPick.setOnClickListener {
            // Launch image picker (allows multiple)
            pickImagesLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val message = etMessage.text.toString().trim()
            if (name.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), "Name and message required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Upload and save (function defined below)
            uploadUrisAndSaveTestimonial(existing, name, message, tempPickedUris) {
                alert.dismiss()
                dialogTvPickedCount = null
            }
        }

        alert.show()
    }

    private fun uploadUrisAndSaveTestimonial(
        existing: Testimonial?,
        name: String,
        message: String,
        uris: List<Uri>,
        onDone: () -> Unit = {}
    ) {
        // Determine Firestore doc reference and Storage folder
        val docRef = existing?.let { firestore.collection("testimonials").document(it.id) }
            ?: firestore.collection("testimonials").document()
        val baseFolder = "testimonials/${docRef.id}"

        // Helper function to save the testimonial document
        fun saveDocument(finalImages: List<String>) {
            val data = mapOf(
                "familyName" to name,
                "message" to message,
                "images" to finalImages,
                "createdAt" to (existing?.createdAt ?: Timestamp.now())
            )
            docRef.set(data)
                .addOnSuccessListener { onDone() }
                .addOnFailureListener { ex ->
                    Toast.makeText(requireContext(), "Failed to save: ${ex.message}", Toast.LENGTH_LONG).show()
                    onDone()
                }
        }

        // If no images picked, save document immediately
        if (uris.isEmpty()) {
            val finalImages = existing?.images ?: emptyList()
            saveDocument(finalImages)
            return
        }

        val uploadedUrls = mutableListOf<String>()
        var finished = 0

        uris.forEach { uri ->
            // Sanitize filename
            val rawName = uri.lastPathSegment ?: "img"
            val cleanName = rawName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val filename = "${System.currentTimeMillis()}_$cleanName"

            val fileRef = storage.reference.child("$baseFolder/$filename")

            // Upload file
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    Log.d("UPLOAD", "Uploaded: ${fileRef.path}")
                    // Get download URL after successful upload
                    fileRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            uploadedUrls.add(downloadUri.toString())
                            finished++
                            if (finished == uris.size) {
                                val finalImages = if (existing != null) existing.images + uploadedUrls else uploadedUrls
                                saveDocument(finalImages)
                            }
                        }
                        .addOnFailureListener { ex ->
                            Log.e("UPLOAD", "Failed to get download URL: ${ex.message}")
                            Toast.makeText(requireContext(), "Failed to get image URL: ${ex.message}", Toast.LENGTH_SHORT).show()
                            finished++
                            if (finished == uris.size) {
                                val finalImages = if (existing != null) existing.images + uploadedUrls else uploadedUrls
                                saveDocument(finalImages)
                            }
                        }
                }
                .addOnFailureListener { ex ->
                    Log.e("UPLOAD", "Upload failed: ${ex.message}")
                    Toast.makeText(requireContext(), "Upload failed: ${ex.message}", Toast.LENGTH_SHORT).show()
                    finished++
                    if (finished == uris.size) {
                        val finalImages = if (existing != null) existing.images + uploadedUrls else uploadedUrls
                        saveDocument(finalImages)
                    }
                }
        }
    }

    private fun confirmAndDelete(t: Testimonial) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete testimonial")
            .setMessage("Delete this testimonial and its images?")
            .setPositiveButton("Delete") { _, _ -> deleteTestimonial(t) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTestimonial(t: Testimonial) {
        val deletions = mutableListOf<Task<Void>>()
        t.images.forEach { url ->
            try {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                deletions.add(ref.delete())
            } catch (e: Exception) {
                // ignore malformed url
            }
        }
        Tasks.whenAllComplete(deletions).addOnSuccessListener {
            firestore.collection("testimonials").document(t.id).delete()
                .addOnSuccessListener { Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show() }
        }.addOnFailureListener {
            // try to delete document even if some deletes failed
            firestore.collection("testimonials").document(t.id).delete()
        }
    }

    private fun deleteImageFromTestimonial(t: Testimonial, imageUrl: String) {
        try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            ref.delete().addOnSuccessListener {
                firestore.collection("testimonials").document(t.id).update("images", FieldValue.arrayRemove(imageUrl))
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid image URL", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
        dialogTvPickedCount = null
    }
}