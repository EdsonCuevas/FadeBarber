package com.example.fadebarber.data.model

data class AppointmentPromotion (
    val id: String = "",
    val idClient: String = "",
    val idEmployee: String = "",
    val idPromotion: Int? = 0,
    val dateAppointment: String = "",
    val timeAppointment: String = "",
    val statusAppointment: Int = 0,
)