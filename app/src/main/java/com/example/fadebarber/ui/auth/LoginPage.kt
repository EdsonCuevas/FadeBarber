package com.example.fadebarber.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginPage(
    viewModel: AuthViewModel,
    onLoginSuccess: (role: Int) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateResetP: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authState = viewModel.authState.observeAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Regex de validaciones
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val passwordRegex = "^(?=.*[0-9]).{8,}$".toRegex()

    val isEmailValid = emailRegex.matches(email)
    val isPasswordValid = passwordRegex.matches(password)

    // Escuchar cambios en el estado de autenticación
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                isProcessing = false
                errorMessage = null
                onLoginSuccess(state.role)
            }
            is AuthState.Error -> {
                isProcessing = false
                errorMessage = state.message
                scope.launch {
                    kotlinx.coroutines.delay(3000)
                    errorMessage = null
                }
            }
            is AuthState.Loading -> {
                isProcessing = true
                errorMessage = null
            }
            is AuthState.EmailSent -> {
            }
            is AuthState.Unauthenticated -> {
            }
            null -> {

            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (errorMessage != null) errorMessage = null
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    email.isEmpty() -> Color.Gray
                    isEmailValid -> Color(0xFF34D399)
                    else -> Color.Red
                },
                unfocusedBorderColor = when {
                    email.isEmpty() -> Color.Gray
                    isEmailValid -> Color(0xFF34D399)
                    else -> Color.Red
                },
                focusedLabelColor = Color(0xFF0EA5E9),
                unfocusedLabelColor = Color.Gray
            )
        )
        if (email.isNotEmpty() && !isEmailValid) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formato de correo electrónico inválido.",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PASSWORD
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (errorMessage != null) errorMessage = null
            },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    password.isEmpty() -> Color.Gray
                    isPasswordValid -> Color(0xFF34D399)
                    else -> Color.Red
                },
                unfocusedBorderColor = when {
                    password.isEmpty() -> Color.Gray
                    isPasswordValid -> Color(0xFF34D399)
                    else -> Color.Red
                },
                focusedLabelColor = Color(0xFF0EA5E9),
                unfocusedLabelColor = Color.Gray
            )
        )
        if (password.isNotEmpty() && !isPasswordValid) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Formato de contraseña inválido.",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animación de error unificada
        AnimatedContent(targetState = errorMessage != null) { showError ->
            if (showError) {
                Text(
                    text = errorMessage ?: "Error de autenticación",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN LOGIN
        Button(
            onClick = {
                if (isEmailValid && isPasswordValid) {
                    viewModel.login(email, password)
                } else {
                    if (!isEmailValid && !isPasswordValid) {
                        errorMessage = "Correo y contraseña inválidos"
                    } else if (!isEmailValid) {
                        errorMessage = "Correo inválido"
                    } else if (!isPasswordValid) {
                        errorMessage = "Contraseña inválida"
                    }

                    scope.launch {
                        kotlinx.coroutines.delay(3000)
                        errorMessage = null
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2)),
            enabled = !isProcessing
        ) {
            Text(
                text = if (isProcessing) "Iniciando sesión..." else "Iniciar sesión",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                errorMessage = null
                viewModel.prepareForSignUp()
                onNavigateToSignUp()
            }
        ) {
            Text("¿No tienes cuenta? Regístrate", fontSize = 14.sp, color = Color(0xFF0EA5E9))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { onNavigateResetP() }) {
            Text("¿Olvidaste tu contraseña?", fontSize = 14.sp, color = Color(0xFF0EA5E9))
        }
    }
}