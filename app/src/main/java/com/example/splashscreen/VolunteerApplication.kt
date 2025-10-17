package com.example.splashscreen

data class VolunteerApplication(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val availability: String = "",
    val status: String = "pending",
    val why: String = "",
    val experience: String = ""
)
