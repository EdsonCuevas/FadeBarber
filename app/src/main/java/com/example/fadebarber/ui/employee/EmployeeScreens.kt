package com.example.fadebarber.ui.employee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.DashboardPage

@Composable
fun EmployeeScreens(route: String) {
    when (route) {
        "dashboard" -> DashboardPage()
        "account" -> {

            val authViewModel: AuthViewModel = viewModel() // Obtenemos el AuthViewModel
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Pantalla Cuenta Cliente", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        authViewModel.logout() // 🔹 Llamamos al logout
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
        "date" -> CitaPage()
    }
}