package com.example.fadebarber.data.model

sealed class HomeTab(val title: String) {
    object Servicios : HomeTab("Servicios")
    object Combos : HomeTab("Combos")
    object Nosotros : HomeTab("Nosotros")
}
