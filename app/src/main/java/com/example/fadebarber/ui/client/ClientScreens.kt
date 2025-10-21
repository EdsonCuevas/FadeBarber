package com.example.fadebarber.ui.client

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.pages.AgendaPage
import com.example.fadebarber.ui.client.pages.CitaPageClient
import com.example.fadebarber.ui.client.pages.CuentaPage
import com.example.fadebarber.ui.client.pages.HomePage

@Composable
fun ClientScreens(
    route: String,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    navController: androidx.navigation.NavHostController,
    isGuest: Boolean = false
) {
    val user = homeViewModel.currentUser.collectAsState().value
    val guestUser = UserData(
        id = "",
        nameUser = "Invitado",
        activeUser = false,
        categoryUser = 1
    )

    when (route) {
        "home" -> {
            if (user != null) {
                HomePage(user = user, viewModel = homeViewModel, navController = navController)
            } else if (isGuest) {
                HomePage(user = guestUser, viewModel = homeViewModel, navController = navController)
            } else {
                LoadingIndicator()
            }
        }
        "account" -> {
            if (user != null) {
                CuentaPage(authViewModel = authViewModel)
            } else if (isGuest) {
                GuestGateScreen(navController = navController)
            } else {
                LoadingIndicator()
            }
        }
        "date" -> {
            if (user != null) {
                CitaPageClient(user = user)
            } else if (isGuest) {
                GuestGateScreen(navController = navController)
            } else {
                LoadingIndicator()
            }
        }
        "agend" -> {
            if (user != null) {
                AgendaPage(user = user, viewModel = homeViewModel)
            } else if (isGuest) {
                AgendaPage(user = guestUser, viewModel = homeViewModel)
            } else {
                LoadingIndicator()
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun GuestGateScreen(navController: androidx.navigation.NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Para acceder a esta sección, crea una cuenta o inicia sesión.")
            Button(onClick = { navController.navigate("login") }) {
                Text(text = "Ir al login")
            }
        }
    }
}
