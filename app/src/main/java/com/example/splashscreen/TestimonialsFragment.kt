package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class TestimonialsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val testimonials = mutableListOf<Testimonial>()
    private lateinit var adapter: TestimonialsAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_testimonials, c, false)
        recycler = v.findViewById(R.id.recyclerTestimonials)
        adapter = TestimonialsAdapter(testimonials, isAdmin = false)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        loadTestimonials()
        return v
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
                if (e != null) return@addSnapshotListener
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.map { doc ->
                    val t = doc.toObject(Testimonial::class.java)!!
                    t.copy(id = doc.id)
                }
                adapter.update(list)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }
}
