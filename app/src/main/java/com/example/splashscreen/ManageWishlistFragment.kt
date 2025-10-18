package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ManageWishlistFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ManageWishlistAdapter
    private lateinit var inputProduct: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnAdd: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val docRef = firestore.collection("wishlist").document("products")
    private var itemsList = mutableListOf<ProductItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_wishlist, container, false)

        inputProduct = view.findViewById(R.id.inputProductName)
        spinnerPriority = view.findViewById(R.id.spinnerPriority)
        btnAdd = view.findViewById(R.id.btnAddProduct)
        recycler = view.findViewById(R.id.recyclerManageWishlist)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = ManageWishlistAdapter(
            itemsList,
            onEdit = { position -> editItem(position) },
            onDelete = { position -> deleteItem(position) }
        )
        recycler.adapter = adapter

        btnAdd.setOnClickListener { addItem() }

        loadWishlist()
        return view
    }

    private fun loadWishlist() {
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(requireContext(), "Error loading wishlist: ${e.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                itemsList.clear()
                adapter.notifyDataSetChanged()
                return@addSnapshotListener
            }

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
        if (!SessionManager.isAdmin) {
            Toast.makeText(requireContext(), "Admin only", Toast.LENGTH_SHORT).show()
            return
        }
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
        if (!SessionManager.isAdmin) return

        val item = itemsList[position]

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_wishlist_item, null)

        val editName = dialogView.findViewById<EditText>(R.id.editProductName)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerEditPriority)

        editName.setText(item.productName)

        val priorities = listOf("High", "Medium", "Low")
        val spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = spinAdapter

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
        if (!SessionManager.isAdmin) return

        val newList = itemsList.map { mapOf("productName" to it.productName, "priority" to it.priority) }
        val data = hashMapOf<String, Any>(
            "items" to newList,
            "_k" to AdminSecrets.ADMIN_KEY   // REQUIRED by Firestore rules
        )
        docRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Wishlist updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(requireContext(), "Failed to save: ${ex.message}", Toast.LENGTH_LONG).show()
            }
    }
}

// Adapter for manage wishlist
class ManageWishlistAdapter(
    private val items: List<ProductItem>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ManageWishlistAdapter.ManageWishlistViewHolder>() {

    class ManageWishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtProduct: TextView = itemView.findViewById(R.id.txtProductManage)
        val txtPriority: TextView = itemView.findViewById(R.id.txtPriorityManage)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageWishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_manage_wishlist_item, parent, false)
        return ManageWishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageWishlistViewHolder, position: Int) {
        val item = items[position]
        holder.txtProduct.text = item.productName
        holder.txtPriority.text = when (item.priority.lowercase()) {
            "high" -> "High Priority"
            "medium" -> "Medium Priority"
            "low" -> "Low Priority"
            else -> item.priority
        }

        if (!SessionManager.isAdmin) {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        }

        holder.btnEdit.setOnClickListener { onEdit(position) }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = items.size
}
