package com.example.fadebarber.data.model

data class AppointmentService (
    val id: String = "",
    val idClient: String = "",
    val idEmployee: String = "",
    val serviceId: Int? = 0,
    val dateAppointment: String = "",
    val timeAppointment: String = "",
    val statusAppointment: Int = 0,
)