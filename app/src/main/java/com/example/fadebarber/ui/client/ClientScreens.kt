package com.example.fadebarber.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.pages.AgendaPage
import com.example.fadebarber.ui.client.pages.CitaPageClient
import com.example.fadebarber.ui.client.pages.CuentaPage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.runtime.collectAsState
import com.example.fadebarber.ui.screens.HomePage

@Composable
fun ClientScreens(
    route: String,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController,
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
                GuestGateScreen(
                    section = "account",
                    onLoginClick = { navController.navigate("login") }
                )
            } else {
                LoadingIndicator()
            }
        }
        "date" -> {
            if (user != null) {
                CitaPageClient(user = user)
            } else if (isGuest) {
                GuestGateScreen(
                    section = "date",
                    onLoginClick = { navController.navigate("login") }
                )
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
private fun GuestGateScreen(
    section: String,
    onLoginClick: () -> Unit
) {
    val (title, description, icon) = when (section) {
        "account" -> Triple(
            "Acceso restringido a Cuenta",
            "Crea una cuenta o inicia sesión para gestionar tu perfil.",
            Icons.Filled.AccountCircle
        )
        "date" -> Triple(
            "Acceso restringido a Citas",
            "Debes iniciar sesión para ver y gestionar tus citas.",
            Icons.Filled.EventNote
        )
        else -> Triple(
            "Acceso restringido",
            "Debes iniciar sesión para continuar.",
            Icons.Filled.EventNote
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = CardDefaults.shape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono principal con degradado acorde a colometría
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0B3DA3),
                                        Color(0xFF2563EB)
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
