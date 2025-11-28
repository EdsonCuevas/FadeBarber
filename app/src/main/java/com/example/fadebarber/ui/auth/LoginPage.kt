package com.example.fadebarber.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginPage(
    viewModel: AuthViewModel,
    onLoginSuccess: (role: Int) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateResetP: () -> Unit
) {
    val authState by viewModel.authState.observeAsState(AuthState.Unauthenticated)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmailError by remember { mutableStateOf(false) }
    var showPasswordError by remember { mutableStateOf(false) }

    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    val passwordRegex = Regex("^.{6,}$")

    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Loading -> isProcessing = true
            is AuthState.Authenticated -> {
                isProcessing = false
                val role = (authState as AuthState.Authenticated).role
                onLoginSuccess(role)
            }
            is AuthState.EmailSent -> {
                isProcessing = false
                errorMessage = "Se envió un correo de verificación. Por favor, revisa tu bandeja de entrada."
            }
            is AuthState.Error -> {
                isProcessing = false
                errorMessage = (authState as AuthState.Error).message
            }
            is AuthState.Guest -> {
                isProcessing = false
                // La navegación al home se maneja por RoleNavGraph
            }
            else -> isProcessing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF1D4ED8)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Logo con fondo circular (encabezado)
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            /**Text(
                text = "Bienvenido",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))
            **/
            Text(
                text = "Inicia sesión para disfrutar \nla experiencia completa",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CARD DE LOGIN (diseño mejorado como ImprovedAppointmentCard)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Acceso a tu cuenta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tus credenciales para acceder",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // EMAIL INPUT
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Correo electrónico",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (errorMessage != null) errorMessage = null
                                showEmailError = false
                            },
                            placeholder = { Text("ejemplo@correo.com", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = if (showEmailError) Color(0xFFEF4444) else Color(0xFF2563EB)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (showEmailError) Color(0xFFEF4444) else Color(0xFF2563EB),
                                unfocusedBorderColor = if (showEmailError) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                focusedLabelColor = Color(0xFF2563EB),
                                unfocusedLabelColor = Color(0xFF64748B),
                                cursorColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD INPUT
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Contraseña",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (errorMessage != null) errorMessage = null
                                showPasswordError = false
                            },
                            placeholder = { Text("••••••••", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (showPasswordError) Color(0xFFEF4444) else Color(0xFF2563EB)
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = image,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (showPasswordError) Color(0xFFEF4444) else Color(0xFF2563EB),
                                unfocusedBorderColor = if (showPasswordError) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                focusedLabelColor = Color(0xFF2563EB),
                                unfocusedLabelColor = Color(0xFF64748B),
                                cursorColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // ERROR MESSAGE
                    AnimatedContent(targetState = errorMessage != null) { showError ->
                        if (showError) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(
                                        Color(0xFFEF4444).copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "Error de autenticación",
                                    color = Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // FORGOT PASSWORD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onNavigateResetP() }) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                fontSize = 13.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // LOGIN BUTTON
                    Button(
                        onClick = {
                            showEmailError = false
                            showPasswordError = false

                            val emailValid = emailRegex.matches(email)
                            val passwordValid = passwordRegex.matches(password)

                            if (emailValid && passwordValid) {
                                viewModel.login(email, password)
                            } else {
                                if (!emailValid && !passwordValid) {
                                    showEmailError = true
                                    showPasswordError = true
                                    errorMessage = "Correo y contraseña inválidos"
                                } else if (!emailValid) {
                                    showEmailError = true
                                    errorMessage = "Correo inválido"
                                } else if (!passwordValid) {
                                    showPasswordError = true
                                    errorMessage = "Contraseña incorrecta. Vuelve a intentarlo o selecciona \"¿Has olvidado tu contraseña?\" para cambiarla."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF2563EB).copy(alpha = 0.6f)
                        ),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        } else {
                            Text(
                                text = "Iniciar sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // DIVIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                        Text(
                            text = "o",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SIGN UP BUTTON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿No tienes cuenta?",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                        TextButton(
                            onClick = {
                                errorMessage = null
                                viewModel.prepareForSignUp()
                                onNavigateToSignUp()
                            }
                        ) {
                            Text(
                                text = "Regístrate",
                                fontSize = 14.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // GUEST LOGIN BUTTON
                    Row(
                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(12.dp)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                errorMessage = null
                                viewModel.loginAsGuest()
                            }
                        ) {
                            Text(
                                text = "Ingresar sin cuenta",
                                fontSize = 14.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2025 Fade Barber",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
