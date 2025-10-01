package com.example.fadebarber.ui.employee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.CuentaV2
import com.example.fadebarber.ui.employee.pages.DashboardPage

@Composable
fun EmployeeScreens(route: String, viewModel: DashboardViewModel = viewModel()) {
    val user = viewModel.currentUser.collectAsState().value
    when (route) {
        "dashboard" -> {
            if (user != null) {
                DashboardPage(user = user, viewModel = viewModel)
            } else {
                Text("CARGANDO...")
            }
        }
        "account" -> CuentaV2()

        "date" -> CitaPage()
    }
}
