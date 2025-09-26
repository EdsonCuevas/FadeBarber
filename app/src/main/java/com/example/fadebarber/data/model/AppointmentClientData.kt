package com.example.fadebarber.data.model

data class AppointmentClientData (
    val id: String = "",
    val idClient: String = "",
    val idEmployee: String = "",
    val serviceId: List<Int?>,
    val idPromotion: List<Int?>,
    val dateAppointment: String = "",
    val timeAppointment: String = "",
    val methodPayment:String = "",
    val totalPrice:Int = 0,
    val statusAppointment: Int = 0,
)