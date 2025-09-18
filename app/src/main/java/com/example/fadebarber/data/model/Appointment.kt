package com.example.fadebarber.data.model

import java.time.LocalDateTime

data class Appointment(
    val id: Int = 0,
    val service: String = "",
    val client: String = "",
    val hour: String = "",
    val date: String = ""
)

data class Date(
    val service: String = "",
    val client: String = "",
)