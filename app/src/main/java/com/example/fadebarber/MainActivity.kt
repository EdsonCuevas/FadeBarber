package com.example.fadebarber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.navegation.RoleNavGraph
import com.example.fadebarber.ui.themes.FadeBarberTheme
import com.example.fadebarber.navegation.UserRole
import com.example.fadebarber.navegation.RoleNavGraph
import com.example.fadebarber.ui.auth.LoginPage
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    // Simulando un rol. Normalmente lo obtienes de login o base de datos
    private var currentUserRole: UserRole = UserRole.EMPLOYEE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aquí le pasamos el rol al grafo de navegación
            RoleNavGraph(role = currentUserRole, authViewModel = authViewModel)

        }
    }
}

