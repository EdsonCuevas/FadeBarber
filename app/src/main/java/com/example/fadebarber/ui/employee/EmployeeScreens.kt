package com.example.fadebarber.ui.employee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.ui.employee.pages.CitaPage
import com.example.fadebarber.ui.employee.pages.CuentaV2
import com.example.fadebarber.ui.employee.pages.DashboardPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreens(
    route: String,
    authViewModel: AuthViewModel,
    viewModel: DashboardViewModel = viewModel()
) {
    val userState = viewModel.currentUser.collectAsState()
    val user = userState.value

    when (route) {
        "dashboard" -> {
            if (user != null) {
                DashboardPage(user = user, viewModel = viewModel)
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
            CuentaV2(authViewModel = authViewModel)
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
                CitaPage(user = user, viewModel = viewModel)
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