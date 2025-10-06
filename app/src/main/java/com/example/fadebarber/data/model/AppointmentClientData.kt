package com.example.fadebarber.data.model

data class AppointmentClientData (
    val id: String? = null,
    val idClient: String? = null,
    val idEmployee: String? = null,
    val serviceId: List<Int?> = emptyList(),
    val idPromotion: List<Int?> = emptyList(),
    val dateAppointment: String? = null,
    val timeAppointment: String? = null,
    val methodPayment:String? = null,
    val nameClient: String? = null,
    val emailClient: String? = null,
    val phoneNumberClient: String? = null,
    val totalPrice:Int? = null,
    val durationTotal: Int? = null,
    val statusAppointment: Int? = null,
)