package com.example.fadebarber.ui.client

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.ui.client.pages.CitaPageClient
import com.example.fadebarber.ui.client.pages.CuentaPage
import com.example.fadebarber.ui.client.pages.HomePage

@Composable
fun ClientScreens(
    route: String,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {
    val user = homeViewModel.currentUser.collectAsState().value

    when (route) {
        "home" -> {
            if (user != null) {
                HomePage(user = user, viewModel = homeViewModel)
            } else {
                LoadingIndicator()
            }
        }
        "account" -> {
            if (user != null) {
                CuentaPage(authViewModel = authViewModel)
            } else {
                LoadingIndicator()
            }
        }
        "date" -> {
            if (user != null) {
                CitaPageClient(user = user)
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
