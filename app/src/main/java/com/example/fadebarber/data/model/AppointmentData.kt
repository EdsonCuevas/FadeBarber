package com.example.fadebarber.data.model


data class AppointmentData(
    val id: String = "",
    val idClient: String = "",
    val idEmployee: String = "",
    val idPromotion: Int = 0,
    val dateAppointment: String = "",
    val serviceId: Int = 0,
    val statusAppointment: Int = 0,
    val clientName: String = "",
    val serviceName: String = "",
    val nameUser: String = "",
    val phoneNumberUser: String = "",
    val correoUser: String = "",
    val timeAppointment: String = "",
)