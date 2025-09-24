package com.example.fadebarber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.navegation.RoleNavGraph
import com.example.fadebarber.navegation.UserRole
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        setContent {
            val authState = authViewModel.authState.observeAsState(initial = AuthState.Unauthenticated).value
            val role = when (authState) {
                is AuthState.Authenticated -> when (authState.role) {
                    //Anonimo a futuro 0
                    1 -> UserRole.CLIENT
                    2 -> UserRole.EMPLOYEE
                    3 -> UserRole.ADMIN

                    else -> UserRole.AUTH
                }
                else -> UserRole.AUTH

            }

            RoleNavGraph(role = role, authViewModel = authViewModel)
        }
    }
}


