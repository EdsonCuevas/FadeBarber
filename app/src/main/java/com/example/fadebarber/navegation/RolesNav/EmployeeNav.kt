package com.example.fadebarber.navegation.RolesNav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.example.fadebarber.navegation.NavigationItem

object EmployeeNav {
    val items = listOf(
        NavigationItem("date", "Dates", Icons.Filled.DateRange),
        NavigationItem("dashboard", "Dashboard", Icons.Filled.Home),
        NavigationItem("account", "Cuenta", Icons.Filled.AccountCircle),

    )
}