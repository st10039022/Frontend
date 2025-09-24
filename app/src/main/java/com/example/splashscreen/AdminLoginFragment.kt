package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminLoginFragment : Fragment() {
    private val adminPassword = "admin123" // Hardcoded admin password

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_admin_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passwordEditText = view.findViewById<EditText>(R.id.et_admin_password)
        val loginButton = view.findViewById<Button>(R.id.btn_admin_login)

        loginButton.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString()

            if (enteredPassword == adminPassword) {
                // Navigate to DashboardActivity
                val intent = Intent(requireContext(), DashboardFragment::class.java)
                startActivity(intent)

                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
