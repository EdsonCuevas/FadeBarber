package com.example.fadebarber.ui.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.ui.client.pages.HomePage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClientScreens(route: String, homeViewModel: HomeViewModel = viewModel()) {
    val user = homeViewModel.currentUser.collectAsState().value

    when (route) {
        "home" -> {
            if (user != null) {
                HomePage(user = user, viewModel =  homeViewModel) // 🔹 Le pasamos también el ViewModel
            } else {
                Text("Cargando información del usuario...")
            }
        }
        "account" -> Text("Pantalla Cuenta Cliente")
        "date" -> Text("Pantalla Citas Cliente")
    }
}
