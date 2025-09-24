package com.example.fadebarber.ui.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
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

        "date" -> Text("Pantalla Citas Cliente")
    }
}
