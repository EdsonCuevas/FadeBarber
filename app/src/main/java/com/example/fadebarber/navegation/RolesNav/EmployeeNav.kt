package com.example.fadebarber.navegation.RolesNav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import com.example.fadebarber.navegation.NavigationItem

object EmployeeNav {
    val items = listOf(
        NavigationItem("dashboard", "Panel", Icons.Filled.Home),
        NavigationItem("date", "Mis Citas", Icons.Filled.ListAlt),
        NavigationItem("agend", "Agendar", Icons.Filled.EventNote),
        NavigationItem("account", "Cuenta", Icons.Filled.AccountCircle),

    )
}