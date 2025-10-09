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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import com.example.fadebarber.data.AuthState
import com.example.fadebarber.data.AuthViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val name by viewModel.registerName.collectAsState()
    val phone by viewModel.registerPhone.collectAsState()
    val email by viewModel.registerEmail.collectAsState()
    val password by viewModel.registerPassword.collectAsState()
    val confirmPassword by viewModel.registerConfirmPassword.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val termsAccepted by viewModel.termsAccepted.collectAsState()
    var registerSuccess by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    // --- NUEVO ESTADO PARA EL MENSAJE DE ERROR ---
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // ---------------------------------------------

    val authState = viewModel.authState.observeAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Validaciones
    val nameRegex = "^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$".toRegex()
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val phoneRegex = "^[0-9]{10}$".toRegex()
    val isNameValid = nameRegex.matches(name)
    val isEmailValid = emailRegex.matches(email)
    val isPhoneValid = phoneRegex.matches(phone)

    // Validaciones de contraseña detalladas
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any {
        it in "!@#$%^&*()_+-=[]{}|;:,.<>?~`\"'\\/<> "
    }
    val isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    val isPasswordMatchValid = password == confirmPassword

    // Validación de formulario completo
    val isFormValid = isNameValid && isEmailValid && isPhoneValid &&
            isPasswordValid && isPasswordMatchValid && termsAccepted

    LaunchedEffect(authState.value) {
        when(val state = authState.value) {
            is AuthState.Authenticated -> {
                isProcessing = false
                registerSuccess = true
                errorMessage = null
            }
            is AuthState.EmailSent -> {
                isProcessing = false
                registerSuccess = true
                errorMessage = null
            }
            is AuthState.Error -> {
                isProcessing = false
                val rawErrorMessage = (state as AuthState.Error).message
                val customErrorMessage = when {
                    rawErrorMessage?.contains("email address is already in use", ignoreCase = true) == true -> {
                        "El correo electrónico ingresado ya está en uso por otro usuario"
                    }
                    else -> rawErrorMessage ?: "Error desconocido"
                }
                errorMessage = customErrorMessage
                registerSuccess = false
            }
            is AuthState.Loading -> {
                isProcessing = true
            }
            else -> {}
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000) // Espera 3 segundos
            errorMessage = null // Limpia el mensaje
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                )
            )
    ) {
        AnimatedContent(
            targetState = registerSuccess,
            modifier = Modifier.fillMaxSize()
        ) { success ->
            if (success) {
                // Pantalla de éxito
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "¡Registro exitoso!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Se ha enviado un correo de verificación a tu email.",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            viewModel.clearRegisterForm()
                            MainScope().launch {
                                delay(150)
                                onNavigateToLogin()
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            "Ir a iniciar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Pantalla de registro
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
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    // Logo con fondo circular
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
                            painter = painterResource(id = R.drawable.logo_goku),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Crear cuenta",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Únete a Fade Barber hoy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Nombre completo
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Nombre completo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { viewModel.updateRegisterName(it) },
                                    placeholder = { Text("Juan Pérez", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = when {
                                                name.isEmpty() -> Color(0xFF2563EB)
                                                isNameValid -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = when {
                                            name.isEmpty() -> Color(0xFF2563EB)
                                            isNameValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        unfocusedBorderColor = when {
                                            name.isEmpty() -> Color(0xFFE2E8F0)
                                            isNameValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (name.isNotEmpty() && !isNameValid) {
                                    Text(
                                        text = "Ingrese un nombre válido (solo letras y espacios)",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            // Teléfono
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Número de teléfono",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() }) viewModel.updateRegisterPhone(it)},
                                    placeholder = { Text("3121234567", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = when {
                                                phone.isEmpty() -> Color(0xFF2563EB)
                                                isPhoneValid -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = when {
                                            phone.isEmpty() -> Color(0xFF2563EB)
                                            isPhoneValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        unfocusedBorderColor = when {
                                            phone.isEmpty() -> Color(0xFFE2E8F0)
                                            isPhoneValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (phone.isNotEmpty() && !isPhoneValid) {
                                    Text(
                                        text = "Ingrese un número válido",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            // Email
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Correo electrónico",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {viewModel.updateRegisterEmail(it)},
                                    placeholder = { Text("ejemplo@correo.com", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Mail,
                                            contentDescription = null,
                                            tint = when {
                                                email.isEmpty() -> Color(0xFF2563EB)
                                                isEmailValid -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = when {
                                            email.isEmpty() -> Color(0xFF2563EB)
                                            isEmailValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        unfocusedBorderColor = when {
                                            email.isEmpty() -> Color(0xFFE2E8F0)
                                            isEmailValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (email.isNotEmpty() && !isEmailValid) {
                                    Text(
                                        text = "Ingrese un email válido",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            // Password
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Contraseña",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { viewModel.updateRegisterPassword(it)},
                                    placeholder = { Text("••••••••", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = when {
                                                password.isEmpty() -> Color(0xFF2563EB)
                                                isPasswordValid -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            }
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
                                                tint = Color(0xFF2563EB)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = when {
                                            password.isEmpty() -> Color(0xFF2563EB)
                                            isPasswordValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        unfocusedBorderColor = when {
                                            password.isEmpty() -> Color(0xFFE2E8F0)
                                            isPasswordValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                // Validaciones visuales
                                if (password.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .background(
                                                Color(0xFFF8FAFC),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "• Al menos 8 caracteres",
                                                fontSize = 11.sp,
                                                color = if (hasMinLength) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "• Al menos 1 Mayúscula",
                                                fontSize = 11.sp,
                                                color = if (hasUppercase) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "• Al menos 1 Minúscula",
                                                fontSize = 11.sp,
                                                color = if (hasLowercase) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "• Al menos 1 Número",
                                                fontSize = 11.sp,
                                                color = if (hasDigit) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "• Al menos 1 carácter especial",
                                                fontSize = 11.sp,
                                                color = if (hasSpecialChar) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            // Confirm Password
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Confirmar contraseña",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { viewModel.updateRegisterConfirmPassword(it) },
                                    placeholder = { Text("••••••••", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = when {
                                                confirmPassword.isEmpty() -> Color(0xFF2563EB)
                                                isPasswordMatchValid -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            }
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
                                                tint = Color(0xFF2563EB)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = when {
                                            confirmPassword.isEmpty() -> Color(0xFF2563EB)
                                            isPasswordMatchValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        unfocusedBorderColor = when {
                                            confirmPassword.isEmpty() -> Color(0xFFE2E8F0)
                                            isPasswordMatchValid -> Color(0xFF10B981)
                                            else -> Color(0xFFEF4444)
                                        },
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (confirmPassword.isNotEmpty() && !isPasswordMatchValid) {
                                    Text(
                                        text = "Las contraseñas no coinciden",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            // Términos y condiciones
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { viewModel.updateTermsAccepted(it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF2563EB),
                                        uncheckedColor = Color(0xFF64748B),
                                        checkmarkColor = Color.White
                                    )
                                )
                                ClickableText(
                                    text = buildAnnotatedString {
                                        append("Acepto los ")
                                        pushStringAnnotation(
                                            tag = "TERMS",
                                            annotation = "terms"
                                        )
                                        withStyle(
                                            style = SpanStyle(
                                                color = Color(0xFF2563EB),
                                                textDecoration = TextDecoration.Underline,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        ) {
                                            append("Términos y Condiciones")
                                        }
                                        pop()
                                        append(" y la ")
                                        pushStringAnnotation(
                                            tag = "PRIVACY",
                                            annotation = "privacy"
                                        )
                                        withStyle(
                                            style = SpanStyle(
                                                color = Color(0xFF2563EB),
                                                textDecoration = TextDecoration.Underline,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        ) {
                                            append("Política de Privacidad")
                                        }
                                        pop()
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp, top = 12.dp),
                                    onClick = { offset ->
                                        val annotatedString = buildAnnotatedString {
                                            append("Acepto los ")
                                            pushStringAnnotation(
                                                tag = "TERMS",
                                                annotation = "terms"
                                            )
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(0xFF2563EB),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append("Términos y Condiciones")
                                            }
                                            pop()
                                            append(" y la ")
                                            pushStringAnnotation(
                                                tag = "PRIVACY",
                                                annotation = "privacy"
                                            )
                                            withStyle(
                                                style = SpanStyle(
                                                    color = Color(0xFF2563EB),
                                                    textDecoration = TextDecoration.Underline
                                                )
                                            ) {
                                                append("Política de Privacidad")
                                            }
                                            pop()
                                        }
                                        annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                                            when (annotation.tag) {
                                                "TERMS" -> onNavigateToTerms()
                                                "PRIVACY" -> onNavigateToPrivacy()
                                            }
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // --- MENSAJE DE ERROR
                            AnimatedContent(targetState = errorMessage != null) { showError ->
                                if (showError) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp) // Espacio encima del mensaje
                                            .background(
                                                Color(0xFFEF4444).copy(alpha = 0.1f), // Fondo rojo claro
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = errorMessage ?: "Error al registrarse",
                                            color = Color(0xFFEF4444),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp)) // Espacio debajo del mensaje
                                }
                            }


                            // Botón de registro
                            Button(
                                onClick = {
                                    errorMessage = null
                                    if (!termsAccepted) {
                                        errorMessage = "Debes aceptar los términos y condiciones"
                                    } else if (!isPasswordMatchValid) {
                                        errorMessage = "Las contraseñas no coinciden"
                                    } else if (!isPasswordValid) {
                                        errorMessage = "La contraseña no cumple con los requisitos"
                                    } else if (!isNameValid) {
                                        errorMessage = "Nombre no válido"
                                    } else if (!isEmailValid) {
                                        errorMessage = "Email no válido"
                                    } else if (!isPhoneValid) {
                                        errorMessage = "Teléfono no válido"
                                    } else {
                                        viewModel.signup(name, email, password, phone)
                                        errorMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFormValid) Color(0xFF2563EB) else Color(0xFF94A3B8),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF94A3B8)
                                ),
                                enabled = isFormValid && !isProcessing
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
                                        "Crear cuenta",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
                            Spacer(modifier = Modifier.height(12.dp))
                            // Ya tienes cuenta
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "¿Ya tienes cuenta?",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B)
                                )
                                TextButton(onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    viewModel.clearRegisterForm()
                                    MainScope().launch {
                                        delay(150)
                                        onNavigateToLogin()
                                        isLoading = false
                                    }
                                }) {
                                    Text(
                                        "Iniciar sesión",
                                        fontSize = 14.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "© 2025 Fade Barber",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
        // Mostrar spinner encima si está cargando
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}