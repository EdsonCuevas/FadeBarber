package com.example.fadebarber.ui.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fadebarber.data.AuthViewModel
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
        "date" -> Text("Pantalla Citas Cliente")
    }
}
