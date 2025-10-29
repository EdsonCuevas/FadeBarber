package com.example.fadebarber.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.example.fadebarber.ui.admin.pages.DashboardAdminScreen

@Composable
fun AdminScreens(route: String) {
    when (route) {
        "dashboard" -> DashboardAdminScreen()
        "employee" -> Text("Pantalla Empleados del Admin")
        "account" -> Text("Pantalla Cuenta Admin")
    }
}
