package com.example.fadebarber.ui.employee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.ui.employee.pages.CuentaV2
import com.example.fadebarber.ui.employee.pages.AgendarEmpleadoPage
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.DashboardPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreens(
    route: String,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    onNavigate: (String) -> Unit = {}
) {
    val userState = dashboardViewModel.currentUser.collectAsState()
    val user = userState.value

    when (route) {
        "dashboard" -> {
            if (user != null) {
                DashboardPage(
                    user = user,
                    viewModel = dashboardViewModel,
                    onNavigateToAccount = { onNavigate("account") }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        "agend" -> {
            if (user != null) {
                AgendarEmpleadoPage(user = user, viewModel = homeViewModel)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        "account" -> {
            if (user != null) {
                CuentaV2(
                    authViewModel = authViewModel,
                    viewModel = dashboardViewModel,
                    onNavigateToTerms = { onNavigate("terms") },
                    onNavigateToPrivacy = { onNavigate("privacy") },
                    onNavigateToFAQ = { onNavigate("faq") },
                    onNavigateToAbout = { onNavigate("about") }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        "date" -> {
            if (user != null) {
                CitaPage(user = user, viewModel = dashboardViewModel)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
