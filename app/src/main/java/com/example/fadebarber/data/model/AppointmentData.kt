package com.example.fadebarber.data.model

data class AppointmentData(
    val id: Int = 0,
    val idClient: Int = 0,
    val idEmployee: Int = 0,
    val idPromotion: Int = 0,
    val dateAppointment: String = "",
    val serviceId: Int = 0, // Cambiado de String a Int
    val statusAppointment: Int = 0,
    val clientName: String = "",
    val serviceName: String = "",
    val nameUser: String = "",
    val time: String = ""
)