package com.example.fadebarber.ui.employee.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.ui.client.components.HorarioItem
import com.example.fadebarber.ui.client.components.SkeletonHorarioItem
import com.example.fadebarber.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaV2(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
    authViewModel: AuthViewModel
) {
    val userState = viewModel.currentUser.collectAsState()
    val user = userState.value
    val context = LocalContext.current

    // Regex patterns
    val phoneRegex = "^[0-9]{7,15}$".toRegex()
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    val passwordRegex = "^(?=.*[0-9]).{8,}$".toRegex()

    // Estados para editar información
    var editableName by remember { mutableStateOf("") }
    var editableEmail by remember { mutableStateOf("") }
    var editablePhone by remember { mutableStateOf("") }

    // Estados originales para restaurar al cancelar
    var originalName by remember { mutableStateOf("") }
    var originalEmail by remember { mutableStateOf("") }
    var originalPhone by remember { mutableStateOf("") }

    // Estados para cambiar contraseña
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCuenta by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showHorario by remember { mutableStateOf(true) }

    // Estados de carga y edición
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingPassword by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isEditingPassword by remember { mutableStateOf(false) }

    // Estados para alertas
    var alertMessage by remember { mutableStateOf("") }
    var alertColor by remember { mutableStateOf(Color(0xFF10B981)) }
    var showAlert by remember { mutableStateOf(false) }

    // Validaciones en tiempo real
    val isEmailValid = editableEmail.isEmpty() || emailRegex.matches(editableEmail)
    val isPhoneValid = editablePhone.isEmpty() || phoneRegex.matches(editablePhone)
    val isNewPasswordValid = newPassword.isEmpty() || passwordRegex.matches(newPassword)

    // Cargar datos del usuario
    LaunchedEffect(user) {
        user?.let {
            editableName = it.nameUser
            editableEmail = it.correoUser
            editablePhone = it.phoneNumberUser
            originalName = it.nameUser
            originalEmail = it.correoUser
            originalPhone = it.phoneNumberUser
        }
        Log.d("EmployeeScreens", "Iniciando configuración FCM...")
        NotificationHelper.saveUserToken()
        Log.d("EmployeeScreens", "Token FCM configurado")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Perfil del usuario
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = editableName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Editar Cuenta Card
            MenuItemWithArrow(
                text = "Editar Cuenta",
                onClick = { showCuenta = !showCuenta }
            )

            // Formulario de edición
            AnimatedVisibility(
                visible = showCuenta,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = editableName,
                                onValueChange = { editableName = it },
                                label = { Text("Nombre") },
                                enabled = isEditingProfile && !isLoading,
                                isError = isEditingProfile && editableName.isBlank(),
                                supportingText = {
                                    if (isEditingProfile && editableName.isBlank()) {
                                        Text("El nombre es requerido", color = Color(0xFFEF4444))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editableEmail,
                                onValueChange = { editableEmail = it },
                                label = { Text("Correo") },
                                enabled = isEditingProfile && !isLoading,
                                isError = isEditingProfile && !isEmailValid,
                                supportingText = {
                                    if (isEditingProfile && !isEmailValid) {
                                        Text("Formato de correo inválido", color = Color(0xFFEF4444))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editablePhone,
                                onValueChange = { editablePhone = it },
                                label = { Text("Teléfono") },
                                enabled = isEditingProfile && !isLoading,
                                isError = isEditingProfile && !isPhoneValid,
                                supportingText = {
                                    if (isEditingProfile && !isPhoneValid) {
                                        Text("Teléfono debe tener 7-15 dígitos", color = Color(0xFFEF4444))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Botones de acción
                            if (!isEditingProfile) {
                                Button(
                                    onClick = { isEditingProfile = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                        contentDescription = "Editar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Editar Información")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            editableName = originalName
                                            editableEmail = originalEmail
                                            editablePhone = originalPhone
                                            isEditingProfile = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6B7280)
                                        )
                                    ) {
                                        Text("Cancelar")
                                    }

                                    Button(
                                        onClick = {
                                            updateUserData(
                                                name = editableName,
                                                email = editableEmail,
                                                phone = editablePhone,
                                                context = context,
                                                viewModel = viewModel,
                                                emailRegex = emailRegex,
                                                phoneRegex = phoneRegex,
                                                onLoadingChange = { isLoading = it },
                                                onSuccess = {
                                                    originalName = editableName
                                                    originalEmail = editableEmail
                                                    originalPhone = editablePhone
                                                    isEditingProfile = false
                                                },
                                                onShowAlert = { message, color ->
                                                    alertMessage = message
                                                    alertColor = color
                                                    showAlert = true
                                                }
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isLoading &&
                                                editableName.isNotBlank() &&
                                                isEmailValid &&
                                                isPhoneValid,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White
                                            )
                                        } else {
                                            Text("Guardar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cambiar Contraseña
            MenuItemWithArrow(
                text = "Cambiar Contraseña",
                onClick = { showPassword = !showPassword }
            )

            AnimatedVisibility(
                visible = showPassword,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            var showCurrentPassword by remember { mutableStateOf(false) }
                            var showNewPassword by remember { mutableStateOf(false) }
                            var showConfirmPassword by remember { mutableStateOf(false) }

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Contraseña Actual") },
                                enabled = isEditingPassword && !isLoadingPassword,
                                isError = isEditingPassword && currentPassword.isBlank(),
                                supportingText = {
                                    if (isEditingPassword && currentPassword.isBlank()) {
                                        Text("La contraseña actual es requerida", color = Color(0xFFEF4444))
                                    }
                                },
                                visualTransformation = if (showCurrentPassword)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword)
                                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showCurrentPassword)
                                                "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva Contraseña") },
                                enabled = isEditingPassword && !isLoadingPassword,
                                isError = isEditingPassword && !isNewPasswordValid && newPassword.isNotEmpty(),
                                supportingText = {
                                    if (isEditingPassword && newPassword.isNotEmpty() && !isNewPasswordValid) {
                                        Text("Mínimo 8 caracteres y 1 número", color = Color(0xFFEF4444))
                                    }
                                },
                                visualTransformation = if (showNewPassword)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = if (showNewPassword)
                                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showNewPassword)
                                                "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar Contraseña") },
                                enabled = isEditingPassword && !isLoadingPassword,
                                isError = isEditingPassword && confirmPassword.isNotEmpty() &&
                                        newPassword != confirmPassword,
                                supportingText = {
                                    if (isEditingPassword && confirmPassword.isNotEmpty() &&
                                        newPassword != confirmPassword) {
                                        Text("Las contraseñas no coinciden", color = Color(0xFFEF4444))
                                    }
                                },
                                visualTransformation = if (showConfirmPassword)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword)
                                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (showConfirmPassword)
                                                "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!isEditingPassword) {
                                Button(
                                    onClick = { isEditingPassword = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                        contentDescription = "Editar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cambiar Contraseña")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            isEditingPassword = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isLoadingPassword,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6B7280)
                                        )
                                    ) {
                                        Text("Cancelar")
                                    }

                                    Button(
                                        onClick = {
                                            updatePassword(
                                                currentPassword = currentPassword,
                                                newPassword = newPassword,
                                                confirmPassword = confirmPassword,
                                                context = context,
                                                userName = editableName,
                                                passwordRegex = passwordRegex,
                                                onLoadingChange = { isLoadingPassword = it },
                                                onPasswordCleared = {
                                                    currentPassword = ""
                                                    newPassword = ""
                                                    confirmPassword = ""
                                                    isEditingPassword = false
                                                },
                                                onShowAlert = { message, color ->
                                                    alertMessage = message
                                                    alertColor = color
                                                    showAlert = true
                                                }
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isLoadingPassword &&
                                                currentPassword.isNotEmpty() &&
                                                newPassword.isNotEmpty() &&
                                                isNewPasswordValid &&
                                                newPassword == confirmPassword,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        )
                                    ) {
                                        if (isLoadingPassword) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White)
                                        } else {
                                            Text("Guardar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gestionar Horario
            Text(
                text = "Horarios laborales",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    visible = showHorario,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    if (user == null) {
                        // Skeleton Loading
                        Column(modifier = Modifier.padding(20.dp)) {
                            repeat(7) { index ->
                                SkeletonHorarioItem()
                                if (index < 6) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val diasOrdenados = listOf(
                                "monday" to "Lunes",
                                "tuesday" to "Martes",
                                "wednesday" to "Miércoles",
                                "thursday" to "Jueves",
                                "friday" to "Viernes",
                                "saturday" to "Sábado",
                                "sunday" to "Domingo"
                            )

                            diasOrdenados.forEachIndexed { index, (diaKey, diaNombre) ->
                                val horario = user.schedule.get(diaKey)
                                horario?.let {
                                    HorarioItem(
                                        isOpen = it.available,
                                        horaInicio = it.start.toString(),
                                        horaFin = it.end.toString(),
                                        diaNombre = diaNombre
                                    )
                                    if (index < diasOrdenados.size - 1) {
                                        Divider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = Color.LightGray.copy(alpha = 0.3f),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cerrar Sesión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Log.d("CuentaPage", "Botón de logout presionado")
                        authViewModel.logout()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Alerta personalizada
        if (showAlert) {
            CustomAlert(
                message = alertMessage,
                color = alertColor,
                onDismiss = { showAlert = false }
            )
        }
    }
}

@Composable
private fun MenuItemWithArrow(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, fontSize = 16.sp, color = Color.Black)
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Ir a $text",
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun CustomAlert(
    message: String,
    color: Color,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = color),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun updateUserData(
    name: String,
    email: String,
    phone: String,
    context: android.content.Context,
    viewModel: DashboardViewModel,
    emailRegex: Regex,
    phoneRegex: Regex,
    onLoadingChange: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onShowAlert: (String, Color) -> Unit
) {
    Log.d("UpdateUser", "Iniciando actualización de datos")
    onLoadingChange(true)

    try {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            onShowAlert("Usuario no autenticado", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        // Validaciones
        if (name.isBlank() || phone.isBlank()) {
            onShowAlert("Por favor completa todos los campos", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        if (!phoneRegex.matches(phone)) {
            onShowAlert("Número de teléfono inválido (7-15 dígitos)", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        val userId = firebaseUser.uid
        val database = FirebaseDatabase.getInstance().getReference("User")

        // Actualizar datos en Realtime Database
        val updates: Map<String, Any> = mapOf(
            "nameUser" to name,
            "phoneNumberUser" to phone
        )

        database.child(userId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Log.d("UpdateUser", "Datos actualizados en Realtime Database")

                // Determinar qué tipo de cambio se hizo y enviar notificación
                val originalUser = viewModel.currentUser.value
                originalUser?.let { original ->
                    when {
                        name != original.nameUser -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "name_change")
                        }
                        phone != original.phoneNumberUser -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "phone_change")
                        }
                        else -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "profile_update")
                        }
                    }
                }

                // No se permite modificar correo desde la app

                // Recargar datos del usuario desde el ViewModel
                viewModel.loadCurrentUser()
                onShowAlert("Datos actualizados correctamente", Color(0xFF10B981))
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("UpdateUser", "Error actualizando Realtime Database", e)
                onShowAlert("Error al actualizar datos: ${e.message}", Color(0xFFEF4444))
            }
            .addOnCompleteListener {
                onLoadingChange(false)
            }

    } catch (e: Exception) {
        Log.e("UpdateUser", "Error general", e)
        onShowAlert("Error inesperado: ${e.message}", Color(0xFFEF4444))
        onLoadingChange(false)
    }
}

private fun updatePassword(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    context: android.content.Context,
    userName: String,
    passwordRegex: Regex,
    onLoadingChange: (Boolean) -> Unit,
    onPasswordCleared: () -> Unit,
    onShowAlert: (String, Color) -> Unit
) {
    Log.d("UpdatePassword", "Iniciando cambio de contraseña")
    onLoadingChange(true)

    try {
        // Validaciones
        if (newPassword != confirmPassword) {
            onShowAlert("Las contraseñas no coinciden", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        if (!passwordRegex.matches(newPassword)) {
            onShowAlert("La contraseña debe tener al menos 8 caracteres y contener al menos un número", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            onShowAlert("Usuario no autenticado", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        // Reautenticar usuario antes de cambiar contraseña
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
            firebaseUser.email ?: "",
            currentPassword
        )

        firebaseUser.reauthenticate(credential)
            .addOnSuccessListener {
                // Actualizar contraseña
                firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Log.d("UpdatePassword", "Contraseña actualizada correctamente")
                        onPasswordCleared()
                        NotificationHelper.sendProfileUpdateNotification(context, userName, "password_change")
                        onShowAlert("Contraseña actualizada correctamente", Color(0xFF10B981))
                        onLoadingChange(false)
                    }
                    .addOnFailureListener { e ->
                        Log.e("UpdatePassword", "Error actualizando contraseña", e)
                        onShowAlert("Error actualizando contraseña: ${e.message}", Color(0xFFEF4444))
                        onLoadingChange(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UpdatePassword", "Error de autenticación", e)
                onShowAlert("Contraseña actual incorrecta", Color(0xFFEF4444))
                onLoadingChange(false)
            }

    } catch (e: Exception) {
        Log.e("UpdatePassword", "Error general", e)
        onShowAlert("Error inesperado: ${e.message}", Color(0xFFEF4444))
        onLoadingChange(false)
    }
}
