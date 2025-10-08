package com.example.splashscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class DropOffZonesFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ManageWishlistAdapter
    private lateinit var inputProduct: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnAdd: Button
    private lateinit var adminControls: LinearLayout

    private val firestore = FirebaseFirestore.getInstance()
    private val docRef = firestore.collection("wishlist").document("products")
    private var itemsList = mutableListOf<ProductItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_drop_off_zones, container, false)

        // Wishlist setup
        recycler = view.findViewById(R.id.recyclerWishlist)
        adminControls = view.findViewById(R.id.adminControls)
        inputProduct = view.findViewById(R.id.inputProductName)
        spinnerPriority = view.findViewById(R.id.spinnerPriority)
        btnAdd = view.findViewById(R.id.btnAddProduct)

        recycler.layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically(): Boolean = false
        }

        adapter = ManageWishlistAdapter(
            items = itemsList,
            onEdit = { position -> editItem(position) },
            onDelete = { position -> deleteItem(position) }
        ).apply {
        }

        recycler.adapter = adapter

        // Show admin controls only if admin
        if (SessionManager.isAdmin) {
            adminControls.visibility = View.VISIBLE
            btnAdd.setOnClickListener { addItem() }
        } else {
            adminControls.visibility = View.GONE
        }

        loadWishlist()
        setupMapAndButtons(view)

        return view
    }

    private fun setupMapAndButtons(view: View) {
        val directionsButton: Button = view.findViewById(R.id.buttonGetDirections)
        directionsButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=03 West Riding Road, Hillcrest, KZN")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        val callButton: Button = view.findViewById(R.id.buttonCallUs)
        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0317651234")
            startActivity(intent)
        }

        val openMaps: View = view.findViewById(R.id.buttonOpenMaps)
        openMaps.setOnClickListener {
            Snackbar.make(view, "Opening in Google Maps…", Snackbar.LENGTH_SHORT).show()
            val gmmIntentUri = Uri.parse("geo:0,0?q=03 West Riding Road, Hillcrest, KZN")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun loadWishlist() {
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            itemsList.clear()
            val items = snapshot.get("items") as? List<Map<String, Any>>
            items?.forEach { map ->
                val product = map["productName"] as? String ?: ""
                val priority = map["priority"] as? String ?: ""
                itemsList.add(ProductItem(product, priority))
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun addItem() {
        val name = inputProduct.text.toString().trim()
        val priority = spinnerPriority.selectedItem.toString().lowercase()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a product name", Toast.LENGTH_SHORT).show()
            return
        }

        itemsList.add(ProductItem(name, priority))
        saveToFirestore()
        inputProduct.text.clear()
    }

    private fun editItem(position: Int) {
        val item = itemsList[position]

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_wishlist_item, null)

        val editName = dialogView.findViewById<EditText>(R.id.editProductName)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerEditPriority)

        editName.setText(item.productName)

        val priorities = listOf("High", "Medium", "Low")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter

        val currentIndex = priorities.indexOfFirst { it.equals(item.priority, ignoreCase = true) }
        if (currentIndex != -1) spinnerPriority.setSelection(currentIndex)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Product")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = editName.text.toString().trim()
                val newPriority = spinnerPriority.selectedItem.toString().lowercase()

                if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "Product name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                itemsList[position] = ProductItem(newName, newPriority)
                saveToFirestore()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }


    private fun deleteItem(position: Int) {
        if (!SessionManager.isAdmin) return
        itemsList.removeAt(position)
        saveToFirestore()
    }

    private fun saveToFirestore() {
        val newList = itemsList.map {
            mapOf("productName" to it.productName, "priority" to it.priority)
        }

        val data = mapOf("items" to newList)

        // Use set() with merge instead of update() for reliability
        docRef.set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Wishlist updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(requireContext(), "Failed to save: ${ex.message}", Toast.LENGTH_LONG).show()
            }
    }
}
