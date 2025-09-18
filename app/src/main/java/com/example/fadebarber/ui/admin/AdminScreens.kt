package com.example.fadebarber.ui.admin
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
@Composable
fun AdminScreens(route: String) {
    when (route) {
        "dashboard" -> Text("Pantalla Dashboard de Admin")
        "employee" -> Text("Pantalla Gestion de Empleado Admin")
        "account" -> Text("Pantalla Cuenta Admin")
    }
}