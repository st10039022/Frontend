package com.example.splashscreen

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    // Epoch millis at start of day (00:00) in device default timezone
    val dateMillis: Long = 0L,
    // Stored as "HH:mm"
    val startTime: String = "",
    val endTime: String = ""
)