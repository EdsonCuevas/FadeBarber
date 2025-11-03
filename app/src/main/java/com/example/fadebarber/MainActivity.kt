package com.example.fadebarber

import android.os.Bundle
import android.util.Log
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.rememberNavController
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.navegation.RoleNavGraph
import com.example.fadebarber.navegation.UserRole
import com.example.fadebarber.services.SecretConfig
import com.example.fadebarber.utils.CloudinaryHelper
//import com.example.fadebarber.ui.theme.FadeBarberTheme
import com.jakewharton.threetenabp.AndroidThreeTen


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        SecretConfig.init(this)
        CloudinaryHelper.initialize(this)

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }

        // DEBUG: Verifica inmediatamente después de init
        Log.d("MainActivity", "Probando obtener SECRET_STRIPE...")
        val testKey = SecretConfig.get("SECRET_STRIPE")
        Log.d("MainActivity", "Resultado: ${if (testKey != null) "✅ FUNCIONA" else "❌ NULL"}")

        setContent {
            val navController = rememberNavController()

            val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
            Log.d("MainActivity", "Estado actual: $authState")


            val role = when (val currentAuthState = authState) {
                is AuthState.Authenticated -> {
                    Log.d("MainActivity", "Rol autenticado: ${currentAuthState.role}")
                    when (currentAuthState.role) {
                        1 -> UserRole.CLIENT
                        2 -> UserRole.EMPLOYEE
                        3 -> UserRole.ADMIN
                        else -> UserRole.AUTH
                    }
                }
                is AuthState.Guest -> {
                    Log.d("MainActivity", "Usuario invitado -> GUEST")
                    UserRole.GUEST
                }
                else -> {
                    Log.d("MainActivity", "Usuario no autenticado -> AUTH")
                    UserRole.AUTH
                }
            }

            RoleNavGraph(
                role = role,
                authViewModel = authViewModel,
                navController = navController
            )
        }
    }
}


