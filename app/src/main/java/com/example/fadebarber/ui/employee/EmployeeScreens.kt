package com.example.fadebarber.ui.employee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.ui.employee.pages.CuentaV2
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.livedata.observeAsState
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.CuentaPage
import com.example.fadebarber.ui.employee.pages.DashboardPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreens(
    route: String,
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = viewModel()
    val userState = viewModel.currentUser.collectAsState()
    val user = userState.value

    when (route) {
        "dashboard" -> {
            val authState = authViewModel.authState.observeAsState()

            when {
                authState.value is AuthState.Unauthenticated -> {
                    // Si no está autenticado, navegar al login
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                user != null -> {
                    DashboardPage(user = user, viewModel = viewModel)
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("CARGANDO...", fontSize = 20.sp)
                    }
                }
            }
        }
        "account" -> CuentaPage()
        "date" -> CitaPage()
    }
}



