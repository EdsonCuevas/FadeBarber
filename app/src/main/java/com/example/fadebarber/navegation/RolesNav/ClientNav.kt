package com.example.fadebarber.navegation.RolesNav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import com.example.fadebarber.navegation.NavigationItem

object ClientNav {
    val items = listOf(
        NavigationItem("home", "Inicio", Icons.Filled.Home),
        NavigationItem("agend", "Agendar", Icons.Filled.EventNote),
        NavigationItem("date", "Historial", Icons.Filled.ListAlt),
        NavigationItem("account", "Cuenta", Icons.Filled.AccountCircle),

    )
}