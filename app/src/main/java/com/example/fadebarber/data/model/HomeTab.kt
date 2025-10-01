package com.example.fadebarber.data.model

sealed class HomeTab(val title: String) {
    object Servicios : HomeTab("Servicios")
    object Promociones : HomeTab("Promociones")
    object Nosotros : HomeTab("Nosotros")
}
