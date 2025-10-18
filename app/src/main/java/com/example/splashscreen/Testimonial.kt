package com.example.splashscreen

import com.google.firebase.Timestamp

data class Testimonial(
    val id: String = "",
    val familyName: String = "",
    val images: List<String> = emptyList(), // download URLs
    val message: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)
