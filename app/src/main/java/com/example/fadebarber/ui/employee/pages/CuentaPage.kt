package com.example.fadebarber.ui.employee.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fadebarber.R
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun CuentaPage(modifier: Modifier = Modifier, navController: NavController = NavController(LocalContext.current), viewModel: DashboardViewModel = viewModel()) {

    val authViewModel: AuthViewModel = viewModel()
    val userState = viewModel.currentUser.collectAsState()
    val user = userState.value
    val context = LocalContext.current

    // Estados para editar información
    var editableName by remember { mutableStateOf("") }
    var editableEmail by remember { mutableStateOf("") }
    var editablePhone by remember { mutableStateOf("") }
    var editablePassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showCuenta by remember { mutableStateOf(true) }
    var showHorario by remember { mutableStateOf(false) }
    var showAyuda by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Estados para alertas personalizadas
    var alertMessage by remember { mutableStateOf("") }
    var alertColor by remember { mutableStateOf(Color(0xFF10B981)) }
    var showAlert by remember { mutableStateOf(false) }

    // Estado para el motivo de ayuda
    var motivoAyuda by remember { mutableStateOf("") }

    // Cargar datos del usuario y configurar notificaciones
    LaunchedEffect(user) {
        user?.let {
            editableName = it.nameUser
            editableEmail = it.correoUser
            editablePhone = it.phoneNumberUser
            editablePassword = ""
        }
        Log.d("EmployeeScreens", "Iniciando configuración FCM...")
        NotificationHelper.saveUserToken()
        Log.d("EmployeeScreens", "Token FCM configurado")
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Perfil mejorado con indicador de edición y gradiente azul
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5), // Azul índigo
                                    Color(0xFF7C3AED)  // Púrpura
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar con indicador de edición
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                )
                                // Indicador de edición
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {

                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user?.nameUser ?: editableName,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = user?.correoUser ?: editableEmail,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.stat_sys_phone_call),
                                        contentDescription = "Teléfono",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = user?.phoneNumberUser ?: editablePhone,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Sección cuenta editable mejorada
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_manage),
                                contentDescription = "Cuenta",
                                tint = Color(0xFF0A66C2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Editar Cuenta",
                                    fontSize = 18.sp,
                                    color = Color(0xFF0A66C2)
                                )
                                Text(
                                    "Actualiza tu información personal",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Datos personales",
                            fontSize = 14.sp,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editableName,
                            onValueChange = { editableName = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF0A66C2)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                            Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editableEmail,
                            onValueChange = { editableEmail = it },
                            label = { Text("Correo") },
                            singleLine = true,
                            enabled = false,
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF0A66C2)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                            Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editablePhone,
                            onValueChange = { editablePhone = it },
                            label = { Text("Teléfono") },
                            singleLine = true,
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF0A66C2)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                            Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editablePassword,
                            onValueChange = { editablePassword = it },
                            label = { Text("Nueva Contraseña (opcional)") },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle contraseña"
                                    )
                                }
                            }
                        )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    Log.d("EmployeeScreens", "Botón Guardar presionado")
                                    Log.d(
                                        "EmployeeScreens",
                                        "Datos actuales: name=$editableName, email=$editableEmail, phone=$editablePhone"
                                    )

                                    updateUserData(
                                        name = editableName,
                                        email = editableEmail,
                                        phone = editablePhone,
                                        password = editablePassword,
                                        context = context,
                                        viewModel = viewModel,
                                        onLoadingChange = { isLoading = it },
                                        onPasswordCleared = { editablePassword = "" },
                                        onShowAlert = { message, color ->
                                            alertMessage = message
                                            alertColor = color
                                            showAlert = true
                                        }
                                    )
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Guardar cambios")
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Horario mejorada
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                contentDescription = "Horario",
                                tint = Color(0xFF0A66C2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Horario",
                                fontSize = 18.sp,
                                color = Color(0xFF0A66C2)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Horarios de Atención",
                                fontSize = 16.sp,
                                color = Color(0xFF374151),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            HorarioItem("Lunes", "7:00 AM", "9:00 PM", true)
                            HorarioItem("Martes", "7:00 AM", "9:00 PM", true)
                            HorarioItem("Miércoles", "7:00 AM", "9:00 PM", true)
                            HorarioItem("Jueves", "7:00 AM", "9:00 PM", true)
                            HorarioItem("Viernes", "7:00 AM", "9:00 PM", true)
                            HorarioItem("Sábado", "8:00 AM", "8:00 PM", true)
                            HorarioItem("Domingo", "Cerrado", "", false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Ayuda
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_help),
                                contentDescription = "Ayuda",
                                tint = Color(0xFF0A66C2),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ayuda",
                                fontSize = 18.sp,
                                color = Color(0xFF0A66C2)
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Motivo De Ayuda",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = motivoAyuda,
                                onValueChange = { motivoAyuda = it },
                                placeholder = { Text("Motivo", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                maxLines = 4,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (motivoAyuda.isNotBlank()) {
                                        alertMessage =
                                            "Solicitud de ayuda enviada correctamente"
                                        alertColor = Color(0xFF10B981)
                                        showAlert = true
                                        motivoAyuda = ""
                                    } else {
                                        alertMessage =
                                            "Por favor describe el motivo de ayuda"
                                        alertColor = Color(0xFFEF4444)
                                        showAlert = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF0A66C2
                                    )
                                )
                            ) {
                                Text("Enviar", color = Color.White)
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        // Botón de cerrar sesión en esquina superior derecha
        IconButton(
            onClick = {
                Log.d("CuentaPage", "Botón de logout presionado")
                authViewModel.logout()
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Cerrar sesión",
                tint = Color(0xFFEF4444)
            )
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


// Componente mejorado para mostrar cada día del horario
@Composable
private fun HorarioItem(dia: String, horaInicio: String, horaFin: String, isOpen: Boolean) {
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
                // Indicador de estado
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
                    text = dia,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOpen && horaFin.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                        contentDescription = "Horario",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$horaInicio - $horaFin",
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
                        text = horaInicio,
                        fontSize = 14.sp,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Componente de alerta personalizada
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

// Función para actualizar datos en Firebase Realtime Database
private fun updateUserData(
    name: String,
    email: String,
    phone: String,
    password: String,
    context: android.content.Context,
    viewModel: DashboardViewModel,
    onLoadingChange: (Boolean) -> Unit,
    onPasswordCleared: () -> Unit,
    onShowAlert: (String, Color) -> Unit
) {
    Log.d("UpdateUser", "Iniciando actualización de datos")
    Log.d("UpdateUser", "Datos recibidos: name=$name, email=$email, phone=$phone")

    onLoadingChange(true)

    try {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            onShowAlert("Usuario no autenticado", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

        val userId = firebaseUser.uid
        val database = FirebaseDatabase.getInstance().getReference("User")

        // Validaciones básicas
        if (name.isBlank() || phone.isBlank()) {
            onShowAlert("Por favor completa todos los campos", Color(0xFFEF4444))
            onLoadingChange(false)
            return
        }

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

                // Actualizar contraseña si se proporcionó
                if (password.isNotEmpty()) {
                    if (password.length < 8) {
                        onShowAlert("La contraseña debe tener al menos 8 caracteres", Color(0xFFEF4444))
                        onLoadingChange(false)
                        return@addOnSuccessListener
                    }

                    firebaseUser.updatePassword(password)
                        .addOnSuccessListener {
                            Log.d("UpdateUser", "Contraseña actualizada")
                            onPasswordCleared()
                            NotificationHelper.sendProfileUpdateNotification(context, name, "password_change")
                            onShowAlert("Contraseña actualizada correctamente", Color(0xFF10B981))
                        }
                        .addOnFailureListener { e ->
                            Log.e("UpdateUser", "Error actualizando contraseña", e)
                            onShowAlert("Error actualizando contraseña: ${e.message}", Color(0xFFEF4444))
                        }
                } else {
                    onPasswordCleared()
                }

                // Recargar datos del usuario desde el ViewModel
                viewModel.loadCurrentUser()
                onShowAlert("Datos actualizados correctamente", Color(0xFF10B981))
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
