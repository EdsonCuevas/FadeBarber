package com.example.fadebarber.ui.employee
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.CuentaPage
import com.example.fadebarber.ui.employee.pages.DashboardPage

@Composable
fun EmployeeScreens(route: String) {
    when (route) {
        "dashboard" -> DashboardPage()
        "account" -> CuentaPage()
        "date" -> CitaPage()
    }
}