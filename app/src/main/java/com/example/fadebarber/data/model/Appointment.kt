package com.example.fadebarber.data.model

import java.time.LocalDateTime

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

data class ServiceData(
    val id: Int = 0,
    val descriptionService : String = "",
    val durationService: Int = 0,
    val imageService: String = "",
    val nameService: String = "",
    val priceService: Int = 0,
    val statusService: Int = 0
)

data class PromotionData(
    val id: Int = 0,
    val durationPromotion: Int = 0,
    val imagePromotion: String = "",
    val namePromotion: String = "",
    val pricePromotion: Int = 0,
    val servicePromotion: List<Int> = emptyList(),
    val statusPromotion: Int = 0
)


data class UserData(
    val id: Int = 0,
    val nameUser: String = "",
    val correoUser: String = "",
    val categoryUser: Int = 0,
    val passwordUser: String = "",
    val phoneNumberUser: String = "",
    val statusUser: Int = 0,
    val activeUser: Boolean = true
)
