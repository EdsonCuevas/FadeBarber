package com.example.fadebarber.ui.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fadebarber.data.AuthViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.fadebarber.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ResetPassword(
    viewModel: AuthViewModel,
    onResetSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    // Banner superior
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var alertColor by remember { mutableStateOf(Color(0xFFEF4444)) }
    var showAlert by remember { mutableStateOf(false) }

    // Loading spinner
    var isLoading by remember { mutableStateOf(false) }

    // Reenviar correo
    var cooldown by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1F66), Color(0xFF2D9CDB)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        AnimatedVisibility(
            visible = showAlert && alertMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter)
                .zIndex(2f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = alertColor),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (alertColor == Color(0xFF10B981)) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alertMessage ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    TextButton(onClick = { showAlert = false }) {
                        Text("✕", color = Color.White)
                    }
                }
            }

            LaunchedEffect(alertMessage) {
                if (alertMessage != null) {
                    delay(2000L)
                    showAlert = false
                }
            }
        }

        // ==== CONTENIDO PRINCIPAL ====
        AnimatedContent(targetState = emailSent) { sent ->
            if (sent) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 200.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Correo enviado correctamente",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onResetSuccess() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF34D399),
                                contentColor = Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Correo Recibido", fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // === BOTÓN DE REENVIAR CORREO ===
                        Button(
                            onClick = {
                                isLoading = true
                                viewModel.resetPassword(email) { success, error ->
                                    isLoading = false
                                    if (success) {
                                        alertMessage = "Correo reenviado correctamente"
                                        alertColor = Color(0xFF10B981)
                                        showAlert = true
                                        cooldown = 30
                                        canResend = false
                                    } else {
                                        alertMessage = error ?: "No se pudo reenviar el correo"
                                        alertColor = Color(0xFFEF4444)
                                        showAlert = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = canResend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canResend) Color(0xFF0A66C2) else Color.Gray,
                                contentColor = Color.White
                            )
                        ) {
                            if (canResend) {
                                Text("Reenviar correo", fontSize = 16.sp)
                            } else {
                                Text("Reenviar en $cooldown s", fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                // card con formulario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 100.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(bottom = 16.dp),
                            alignment = Alignment.Center
                        )
                        Text(
                            text = "Restablecer contraseña",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                when {
                                    email.isEmpty() -> {
                                        alertMessage = "Por favor ingresa un correo"
                                        alertColor = Color(0xFFEF4444)
                                        showAlert = true
                                    }
                                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                        alertMessage = "El formato del correo no es válido"
                                        alertColor = Color(0xFFEF4444)
                                        showAlert = true
                                    }
                                    else -> {
                                        isLoading = true
                                        viewModel.resetPassword(email) { success, error ->
                                            isLoading = false
                                            if (success) {
                                                emailSent = true
                                                alertMessage = "Correo enviado correctamente"
                                                alertColor = Color(0xFF10B981)
                                                showAlert = true
                                                cooldown = 30
                                                canResend = false
                                            } else {
                                                alertMessage = error ?: "No se pudo enviar el correo"
                                                alertColor = Color(0xFFEF4444)
                                                showAlert = true
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0A66C2),
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.MailOutline,
                                    contentDescription = "Enviar correo",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar correo", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { onNavigateToLogin() }) {
                            Text(
                                "Volver al inicio de sesión",
                                fontSize = 14.sp,
                                color = Color(0xFF2D9CDB)
                            )
                        }
                    }
                }
            }
        }

        // ==== LOADING OVERLAY ====
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .align(Alignment.Center)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // ==== EFECTO DE COOLDOWN ====
        LaunchedEffect(emailSent, cooldown) {
            if (emailSent && cooldown > 0) {
                delay(1000L)
                cooldown--
                if (cooldown == 0) {
                    canResend = true
                }
            }
        }
    }
}
