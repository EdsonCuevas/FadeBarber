package com.example.fadebarber.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.ui.admin.pages.DashboardAdminScreen
import com.example.fadebarber.ui.client.pages.CuentaPage

@Composable
fun AdminScreens(route: String, authViewModel: AuthViewModel) {
    when (route) {
        "dashboard" -> DashboardAdminScreen()
        "employee" -> Text("Pantalla Empleados del Admin")
        "account" -> CuentaPage(authViewModel = authViewModel)
    }
}
