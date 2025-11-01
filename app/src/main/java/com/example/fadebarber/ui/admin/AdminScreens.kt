package com.example.fadebarber.ui.admin

import androidx.compose.runtime.Composable
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.ui.admin.pages.AsistenciaScreen
import com.example.fadebarber.ui.admin.pages.DashboardAdmin
import com.example.fadebarber.ui.client.pages.CuentaPage

@Composable
fun AdminScreens(route: String, authViewModel: AuthViewModel) {
    when (route) {
        "dashboard" -> DashboardAdmin()
        "employee" -> AsistenciaScreen()
        "account" -> CuentaPage(authViewModel = authViewModel)
    }
}
