package com.example.fadebarber.navegation.RolesNav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import com.example.fadebarber.navegation.NavigationItem

object ClientNav {
    val items = listOf(
        NavigationItem("date", "Historial", Icons.Filled.DateRange),
        NavigationItem("home", "Inicio", Icons.Filled.Home),
        NavigationItem("account", "Cuenta", Icons.Filled.AccountCircle),

    )
}