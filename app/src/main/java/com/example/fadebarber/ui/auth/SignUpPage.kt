package com.example.fadebarber.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var registerSuccess by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val authState = viewModel.authState.observeAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Validaciones
    val nameRegex = "^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$".toRegex()
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val phoneRegex = "^[0-9]{7,15}$".toRegex()

    val isNameValid = nameRegex.matches(name)
    val isEmailValid = emailRegex.matches(email)
    val isPhoneValid = phoneRegex.matches(phone)

    // Validaciones de contraseña detalladas
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { it == '!' || it == '?' }
    val isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    val isPasswordMatchValid = password == confirmPassword

    LaunchedEffect(authState.value) {
        when(val state = authState.value) {
            is AuthState.Authenticated -> {
                isProcessing = false
                registerSuccess = true
            }
            is AuthState.EmailSent -> {
                isProcessing = false
                registerSuccess = true
            }
            is AuthState.Error -> {
                isProcessing = false
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
                registerSuccess = false
            }
            is AuthState.Loading -> {
                isProcessing = true
            }
            is AuthState.Unauthenticated -> {
            }
            null -> {
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A66C2), // Azul principal
                        Color(0xFF1E40AF)  // Azul más oscuro
                    )
                )
            )
    ) {
       /** Image(
            painter = painterResource(id = R.drawable.tijeras_24),
            contentDescription = "Scissors",
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(70.dp)
                .padding(20.dp)
                .background(Color.White.copy(alpha = 0.0f))
        )

        Image(
            painter = painterResource(id = R.drawable.peluqueria_24),
            contentDescription = "Barber chair",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(70.dp)
                .padding(25.dp)
                .background(Color.White.copy(alpha = 0.0f))
        )

        **/
        AnimatedContent(targetState = registerSuccess) { success ->
            if (success) {
                // Pantalla de éxito
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = Color.White,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "¡Registro exitoso!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Se ha enviado un correo de verificación a tu email.",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onNavigateToLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0A66C2)
                        )
                    ) {
                        Text("Ir a iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                // Pantalla de registro (formulario con scroll)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                            }
                        }
                        .imePadding()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo/Header section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(180.dp)
                                .padding(bottom = 8.dp),
                            alignment = Alignment.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Crear cuenta",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Regístrate para comenzar a usar Fade Barber",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Nombre completo
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre completo") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (name.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = when {
                                        name.isEmpty() -> Color.Gray
                                        isNameValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    unfocusedBorderColor = when {
                                        name.isEmpty() -> Color.Gray
                                        isNameValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    focusedLabelColor = Color(0xFF0A66C2),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTrailingIconColor = Color(0xFF0A66C2),
                                    unfocusedTrailingIconColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (name.isNotEmpty() && !isNameValid) {
                                Text(
                                    text = "Ingrese un nombre válido (solo letras y espacios)",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }

                            // Teléfono
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) phone = it },
                                label = { Text("Número de teléfono") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = if (phone.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = when {
                                        phone.isEmpty() -> Color.Gray
                                        isPhoneValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    unfocusedBorderColor = when {
                                        phone.isEmpty() -> Color.Gray
                                        isPhoneValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    focusedLabelColor = Color(0xFF0A66C2),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTrailingIconColor = Color(0xFF0A66C2),
                                    unfocusedTrailingIconColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (phone.isNotEmpty() && !isPhoneValid) {
                                Text(
                                    text = "Ingrese un número válido (7-15 dígitos)",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (email.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                    )
                                },
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
                                    focusedLabelColor = Color(0xFF0A66C2),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTrailingIconColor = Color(0xFF0A66C2),
                                    unfocusedTrailingIconColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (email.isNotEmpty() && !isEmailValid) {
                                Text(
                                    text = "Ingrese un email válido",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }

                            // Password
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (password.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (password.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                        )
                                    }
                                },
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
                                    focusedLabelColor = Color(0xFF0A66C2),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTrailingIconColor = Color(0xFF0A66C2),
                                    unfocusedTrailingIconColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Validaciones visuales
                            if (password.isNotEmpty()) {
                                Column(modifier = Modifier.align(Alignment.Start)) {
                                    Text(
                                        text = "• Al menos 8 caracteres.",
                                        fontSize = 11.sp,
                                        color = if (hasMinLength) Color(0xFF34D399) else Color.Red
                                    )
                                    Text(
                                        text = "• Al menos 1 Mayúscula.",
                                        fontSize = 11.sp,
                                        color = if (hasUppercase) Color(0xFF34D399) else Color.Red
                                    )
                                    Text(
                                        text = "• Al menos 1 Minúscula.",
                                        fontSize = 11.sp,
                                        color = if (hasLowercase) Color(0xFF34D399) else Color.Red
                                    )
                                    Text(
                                        text = "• Al menos 1 Número.",
                                        fontSize = 11.sp,
                                        color = if (hasDigit) Color(0xFF34D399) else Color.Red
                                    )
                                    Text(
                                        text = "• Al menos 1 carácter especial (! o ?).",
                                        fontSize = 11.sp,
                                        color = if (hasSpecialChar) Color(0xFF34D399) else Color.Red
                                    )
                                }
                            }

                            // Confirm Password
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (confirmPassword.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (confirmPassword.isEmpty()) Color.Gray else Color(0xFF0A66C2)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = when {
                                        confirmPassword.isEmpty() -> Color.Gray
                                        isPasswordMatchValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    unfocusedBorderColor = when {
                                        confirmPassword.isEmpty() -> Color.Gray
                                        isPasswordMatchValid -> Color(0xFF34D399)
                                        else -> Color.Red
                                    },
                                    focusedLabelColor = Color(0xFF0A66C2),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTrailingIconColor = Color(0xFF0A66C2),
                                    unfocusedTrailingIconColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (confirmPassword.isNotEmpty() && !isPasswordMatchValid) {
                                Text(
                                    text = "Las contraseñas no coinciden",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isPasswordMatchValid) {
                                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    } else if (!isPasswordValid) {
                                        Toast.makeText(
                                            context,
                                            "La contraseña no cumple con los requisitos",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else if (!isNameValid) {
                                        Toast.makeText(context, "Nombre no válido", Toast.LENGTH_SHORT).show()
                                    } else if (!isEmailValid) {
                                        Toast.makeText(context, "Email no válido", Toast.LENGTH_SHORT).show()
                                    } else if (!isPhoneValid) {
                                        Toast.makeText(context, "Teléfono no válido", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.signup(name, email, password, phone)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0A66C2),
                                    contentColor = Color.White
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
                                    Text("Crear cuenta", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            TextButton(onClick = { onNavigateToLogin() }) {
                                Text("Ir a iniciar sesión", fontSize = 13.sp, color = Color(0xFF0A66C2), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "© 2025 Fade Barber",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}