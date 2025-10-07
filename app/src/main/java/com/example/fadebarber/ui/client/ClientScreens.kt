package com.example.fadebarber.ui.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.ui.client.pages.CuentaPage
import com.example.fadebarber.ui.client.pages.HomePage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClientScreens(
    route: String,
    authViewModel: AuthViewModel,
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val user = homeViewModel.currentUser.collectAsState().value
    val authViewModel: AuthViewModel = viewModel()
    val authState = authViewModel.authState.observeAsState()
    val viewModel: DashboardViewModel = viewModel()

    when (route) {
        "home" -> {
            if (user != null) {
                HomePage(user = user, viewModel = homeViewModel, authViewModel = authViewModel)
            } else {
                Text("Cargando información del usuario...")
            }
        }
        "account" -> {
            if (user != null) {
                CuentaPage(authViewModel = authViewModel)
            } else {
                Text("Cargando información del usuario...")
            }
        }

        "date" -> {
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
                    com.example.fadebarber.ui.client.pages.CitaPageClient(
                        user = user,
                        viewModel = viewModel
                    )
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
    }
}
