package com.example.fadebarber.ui.employee.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaV2(
    modifier: Modifier = Modifier,
    navController: NavController = NavController(LocalContext.current),
    viewModel: DashboardViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = viewModel()
    val userState = viewModel.currentUser.collectAsState()
    val user = userState.value
    val context = LocalContext.current

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
            // -------------------
            // Perfil del usuario
            // -------------------
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
                Text(text = editableEmail, fontSize = 14.sp, color = Color.Gray)
                Text(text = editablePhone, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -------------------
            // Editar Cuenta Card
            // -------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCuenta = !showCuenta },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Editar Cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = if (showCuenta) "Ocultar formulario" else "Actualiza tu información personal",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // -------------------
            // Formulario de edición
            // -------------------
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
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editableEmail,
                                onValueChange = { editableEmail = it },
                                label = { Text("Correo") },
                                enabled = isEditingProfile && !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editablePhone,
                                onValueChange = { editablePhone = it },
                                label = { Text("Teléfono") },
                                enabled = isEditingProfile && !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Botones de acción
                            if (!isEditingProfile) {
                                // Botón Editar
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
                                // Botones Guardar y Cancelar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Botón Cancelar
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

                                    // Botón Guardar
                                    Button(
                                        onClick = {
                                            updateUserData(
                                                name = editableName,
                                                email = editableEmail,
                                                phone = editablePhone,
                                                context = context,
                                                viewModel = viewModel,
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
                                        enabled = !isLoading,
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

            // -------------------
            // Información Personal
            // -------------------
            InfoPersonalSection(
                editableName = editableName,
                editableEmail = editableEmail,
                editablePhone = editablePhone
            )

            Spacer(modifier = Modifier.height(8.dp))

            // -------------------
            // Cambiar Contraseña
            // -------------------
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

                            // Botones de acción
                            if (!isEditingPassword) {
                                // Botón Editar
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
                                // Botones Guardar y Cancelar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Botón Cancelar
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

                                    // Botón Guardar
                                    Button(
                                        onClick = {
                                            updatePassword(
                                                currentPassword = currentPassword,
                                                newPassword = newPassword,
                                                confirmPassword = confirmPassword,
                                                context = context,
                                                userName = editableName,
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
                                                newPassword.isNotEmpty() &&
                                                newPassword == confirmPassword &&
                                                currentPassword.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        )
                                    ) {
                                        if (isLoadingPassword) {
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

            Spacer(modifier = Modifier.height(32.dp))

            // -------------------
            // Gestionar Horario
            // -------------------
            Text(
                text = "Gestionar Horario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    visible = showHorario,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        val diasOrdenados = listOf(
                            "monday", "tuesday", "wednesday",
                            "thursday", "friday", "saturday", "sunday"
                        )

                        diasOrdenados.forEach { dia ->
                            val horario = user?.schedule?.get(dia)
                            horario?.let {
                                HorarioItem(
                                    isOpen = it.available,
                                    horaInicio = it.start,
                                    horaFin = it.end,
                                    dia = dia
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -------------------
            // Cerrar Sesión
            // -------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
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

// -------------------
// InfoPersonalSection
// -------------------
@Composable
private fun InfoPersonalSection(
    editableName: String,
    editableEmail: String,
    editablePhone: String
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "arrowRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
            Text("Información Personal", fontSize = 16.sp, color = Color.Black)

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.rotate(rotation)
            )
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = editableName,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editableEmail,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Correo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editablePhone,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -------------------
// Menús simples
// -------------------
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
private fun MenuItemWithIcon(icon: ImageVector, text: String, iconColor: Color, onClick: () -> Unit) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = text, fontSize = 16.sp, color = Color.Black)
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Ir a $text",
                tint = Color.Gray
            )
        }
    }
}

// -------------------
// HorarioItem
// -------------------
@Composable
fun HorarioItem(
    isOpen: Boolean,
    horaInicio: String?,
    horaFin: String?,
    dia: String
) {
    val diaEsp = when (dia) {
        "monday" -> "Lunes"
        "tuesday" -> "Martes"
        "wednesday" -> "Miércoles"
        "thursday" -> "Jueves"
        "friday" -> "Viernes"
        "saturday" -> "Sábado"
        "sunday" -> "Domingo"
        else -> dia
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen) Color(0xFFF0F9FF) else Color(0xFFFEF2F2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOpen) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = diaEsp,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOpen && !horaFin.isNullOrEmpty()) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                        contentDescription = "Horario",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${horaInicio} - ${horaFin}",
                        fontSize = 14.sp,
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_lock_power_off),
                        contentDescription = "Cerrado",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Descanso",
                        fontSize = 14.sp,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// -------------------
// Alerta personalizada
// -------------------
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

// -------------------
// Función para actualizar datos del usuario
// -------------------
private fun updateUserData(
    name: String,
    email: String,
    phone: String,
    context: android.content.Context,
    viewModel: DashboardViewModel,
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
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            onShowAlert("Por favor completa todos los campos", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onShowAlert("Correo electrónico inválido", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        val userId = firebaseUser.uid
        val database = FirebaseDatabase.getInstance().getReference("User")

        // Actualizar datos en Realtime Database
        val updates: Map<String, Any> = mapOf(
            "nameUser" to name,
            "correoUser" to email,
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
                        email != original.correoUser -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "email_change")
                        }
                        phone != original.phoneNumberUser -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "phone_change")
                        }
                        else -> {
                            NotificationHelper.sendProfileUpdateNotification(context, name, "profile_update")
                        }
                    }
                }

                // Actualizar email en Firebase Auth si cambió
                if (email != firebaseUser.email) {
                    firebaseUser.verifyBeforeUpdateEmail(email)
                        .addOnSuccessListener {
                            Log.d("UpdateUser", "Verificación de email enviada")
                            onShowAlert("Se ha enviado un email de verificación para cambiar el correo", Color(0xFF0EA5E9))
                        }
                        .addOnFailureListener { e ->
                            Log.e("UpdateUser", "Error enviando verificación de email", e)
                            onShowAlert("Error con verificación de email: ${e.message}", Color(0xFFEF4444))
                        }
                }

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

// -------------------
// Función para actualizar contraseña
// -------------------
private fun updatePassword(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    context: android.content.Context,
    userName: String,
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

        if (newPassword.length < 8) {
            onShowAlert("La contraseña debe tener al menos 8 caracteres", Color(0xFFEF4444))
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