package com.example.fadebarber.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.example.fadebarber.ui.admin.pages.EmployeeListScreen

@Composable
fun AdminScreens(route: String) {
    when (route) {
        "dashboard" -> Text("Pantalla Dashboard de Admin")
        "employee" -> EmployeeListScreen()
        "account" -> Text("Pantalla Cuenta Admin")
    }
}
