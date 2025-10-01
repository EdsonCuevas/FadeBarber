package com.example.fadebarber.data.model

data class PromotionData(
    val durationPromotion: Int? = null,
    val id: Int? = null,
    val imagePromotion: String? = null,
    val namePromotion: String? = null,
    val pricePromotion: Int = 0,
    val servicePromotion: List<Int>? = null, // 🔹 lista de ids de servicios
    val statusPromotion: Int? = null         // 🔹 estado (activo/inactivo)
)
