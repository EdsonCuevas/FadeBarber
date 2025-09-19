package com.example.fadebarber.navegation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.navegation.RolesNav.AdminNav
import com.example.fadebarber.navegation.RolesNav.ClientNav
import com.example.fadebarber.navegation.RolesNav.EmployeeNav
import com.example.fadebarber.ui.admin.AdminScreens
import com.example.fadebarber.ui.auth.LoginPage
import com.example.fadebarber.ui.auth.SignUpPage
import com.example.fadebarber.ui.client.ClientScreens
import com.example.fadebarber.ui.employee.EmployeeScreens


enum class UserRole {CLIENT, EMPLOYEE, ADMIN, AUTH}

@Composable
fun RoleNavGraph(role: UserRole, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val items = when(role) {
        UserRole.CLIENT -> ClientNav.items
        UserRole.EMPLOYEE -> EmployeeNav.items
        UserRole.ADMIN -> AdminNav.items
        UserRole.AUTH -> emptyList()
    }
    val startDestination = when (role) {
        UserRole.CLIENT -> "home"           // Apunta a home
        UserRole.EMPLOYEE -> "dashboard"    // Dashboard para empleados
        UserRole.ADMIN -> "dashboard"
        UserRole.AUTH  -> "login" // Solo para AUTH
    }

    Scaffold(
        bottomBar = { if (items.isNotEmpty()) BottomBar(navController, items) }
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
                    onNavigateToSignUp = { navController.navigate("signup") }
                )
            }

            // Signup
            composable("signup") {
                SignUpPage(
                    viewModel = authViewModel,
                    onLoginSuccess = {},
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                )
            }

            items.forEach { item ->
                composable(item.route) {
                    when (role) {
                        UserRole.CLIENT -> ClientScreens(item.route)
                        UserRole.EMPLOYEE -> EmployeeScreens(item.route)
                        UserRole.ADMIN -> AdminScreens(item.route)
                        else -> {}
                    }
                }
            }
        }
    }

}

