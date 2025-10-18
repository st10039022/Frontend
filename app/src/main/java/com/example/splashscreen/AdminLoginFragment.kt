package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminLoginFragment : Fragment() {

    private val ADMIN_PASSWORD = "admin123" // hardcoded only

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etPassword = view.findViewById<EditText>(R.id.et_admin_password)
        val btnLogin   = view.findViewById<Button>(R.id.btn_admin_login)

        btnLogin.setOnClickListener {
            val entered = etPassword.text?.toString()?.trim() ?: ""
            if (entered.isEmpty()) {
                Toast.makeText(requireContext(), "Enter admin password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (entered == ADMIN_PASSWORD) {
                SessionManager.isAdmin = true
                Toast.makeText(requireContext(), "Welcome Admin!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardFragment())
                    .commit()
            } else {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
