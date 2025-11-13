package com.example.fadebarber.navegation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.navegation.RolesNav.AdminNav
import com.example.fadebarber.navegation.RolesNav.ClientNav
import com.example.fadebarber.navegation.RolesNav.EmployeeNav
import com.example.fadebarber.ui.admin.AdminScreens
import com.example.fadebarber.ui.auth.LoginPage
import com.example.fadebarber.ui.auth.PrivacyPolicyScreen
import com.example.fadebarber.ui.auth.ResetPassword
import com.example.fadebarber.ui.auth.SignUpPage
import com.example.fadebarber.ui.auth.TermsAndConditionsScreen
import com.example.fadebarber.ui.client.ClientScreens
import com.example.fadebarber.ui.employee.EmployeeScreens

enum class UserRole { CLIENT, EMPLOYEE, ADMIN, AUTH, GUEST }

@Composable
fun RoleNavGraph(role: UserRole, authViewModel: AuthViewModel, navController: NavHostController) {

    val items = when (role) {
        UserRole.CLIENT -> ClientNav.items
        UserRole.EMPLOYEE -> EmployeeNav.items
        UserRole.ADMIN -> AdminNav.items
        UserRole.AUTH -> emptyList()
        UserRole.GUEST -> ClientNav.items
    }

    val startDestination = when (role) {
        UserRole.CLIENT -> "home"           // Apunta a home
        UserRole.EMPLOYEE -> "dashboard"    // Dashboard para empleados
        UserRole.ADMIN -> "dashboard"
        UserRole.AUTH -> "login"            // Solo para AUTH
        UserRole.GUEST -> "home"
    }

    // Al cambiar de rol a uno autenticado, redirige al destino inicial correspondiente
    LaunchedEffect(role) {
        when (role) {
            UserRole.CLIENT -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            UserRole.EMPLOYEE, UserRole.ADMIN -> {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }
            UserRole.GUEST -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> { /* AUTH no navega automáticamente */ }
        }
    }

    // Ocultar barra inferior en rutas de autenticación
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val authRoutes = setOf("login", "signup", "resetpassword", "terms", "privacy")

    Scaffold(
        bottomBar = {
            if (items.isNotEmpty() && (currentRoute !in authRoutes)) {
                BottomBar(navController, items)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login
            composable("login") {
                LoginPage(
                    viewModel = authViewModel,
                    onLoginSuccess = {},
                    onNavigateToSignUp = { navController.navigate("signup") },
                    onNavigateResetP = {
                        navController.navigate("resetpassword")
                    }
                )
            }

            // Signup
            composable("signup") {
                SignUpPage(
                    viewModel = authViewModel,
                    onRegisterSuccess = {},
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    },
                    onNavigateToTerms = { navController.navigate("terms") },
                    onNavigateToPrivacy = { navController.navigate("privacy") }
                )
            }

            // Términos y Condiciones
            composable("terms") {
                TermsAndConditionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Política de Privacidad
            composable("privacy") {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Reset Password
            composable("resetpassword") {
                ResetPassword(
                    viewModel = authViewModel,
                    onResetSuccess = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true } // Cambiado de 0 a "login"
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true } // Cambiado de 0 a "login"
                        }
                    }
                )
            }

            items.forEach { item ->
                composable(item.route) { backStackEntry ->
                    when (role) {
                        UserRole.CLIENT -> {
                            // ✅ Recordamos el backStackEntry asociado a "home" una sola vez
                            val parentEntry = remember(backStackEntry) {
                                navController.getBackStackEntry("home")
                            }

                            // ✅ ViewModel compartido entre pantallas del cliente
                            val homeViewModel: HomeViewModel = viewModel(parentEntry)

                            ClientScreens(
                                route = item.route,
                                authViewModel = authViewModel,
                                homeViewModel = homeViewModel,
                                navController = navController
                            )
                        }

                        UserRole.GUEST -> {
                            // Compartir HomeViewModel también para invitados
                            val parentEntry = remember(backStackEntry) {
                                navController.getBackStackEntry("home")
                            }
                            val homeViewModel: HomeViewModel = viewModel(parentEntry)

                            ClientScreens(
                                route = item.route,
                                authViewModel = authViewModel,
                                homeViewModel = homeViewModel,
                                navController = navController,
                                isGuest = true
                            )
                        }

                        UserRole.EMPLOYEE -> EmployeeScreens(item.route, authViewModel)
                        UserRole.ADMIN -> AdminScreens(item.route, authViewModel, onNavigate = { newRoute -> navController.navigate(newRoute) }
                            )
                        else -> {}
                    }
                }
            }
        }
    }
}