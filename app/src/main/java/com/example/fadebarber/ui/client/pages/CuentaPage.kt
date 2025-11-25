package com.example.fadebarber.ui.client.pages

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.utils.CloudinaryHelper
import com.example.fadebarber.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaPage(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
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
    var profileImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

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

    // Estados de carga y edición
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingPassword by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isEditingPassword by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }

    // Estados para alertas
    var alertMessage by remember { mutableStateOf("") }
    var alertColor by remember { mutableStateOf(Color(0xFF10B981)) }
    var showAlert by remember { mutableStateOf(false) }

    // Validaciones en tiempo real
    val isEmailValid = editableEmail.isEmpty() || emailRegex.matches(editableEmail)
    val isPhoneValid = editablePhone.isEmpty() || phoneRegex.matches(editablePhone)
    val isNewPasswordValid = newPassword.isEmpty() || passwordRegex.matches(newPassword)

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@let

            // Subir imagen a Cloudinary
            CloudinaryHelper.uploadProfileImage(
                context = context,
                imageUri = it,
                userId = userId,
                onSuccess = { downloadUrl ->
                    profileImageUrl = downloadUrl
                    updateProfileImageUrl(
                        imageUrl = downloadUrl,
                        viewModel = viewModel,
                        onShowAlert = { message, color ->
                            alertMessage = message
                            alertColor = color
                            showAlert = true
                        }
                    )
                    isUploadingImage = false
                    uploadProgress = 0
                },
                onError = { error ->
                    alertMessage = error
                    alertColor = Color(0xFFEF4444)
                    showAlert = true
                    isUploadingImage = false
                    uploadProgress = 0
                },
                onProgress = { progress ->
                    uploadProgress = progress
                    if (progress > 0 && !isUploadingImage) {
                        isUploadingImage = true
                    }
                }
            )
        }
    }

    // Inicializar Cloudinary
    LaunchedEffect(Unit) {
        CloudinaryHelper.initialize(context)
    }

    // Cargar datos del usuario
    LaunchedEffect(Unit, user) {
        Log.d("CuentaPage", "CuentaPage se está mostrando")

        user?.let {
            editableName = it.nameUser
            editableEmail = it.correoUser
            editablePhone = it.phoneNumberUser
            profileImageUrl = it.photoURL
            originalName = it.nameUser
            originalEmail = it.correoUser
            originalPhone = it.phoneNumberUser
        }

        Log.d("EmployeeScreens", "Iniciando configuración FCM...")
        NotificationHelper.saveUserToken()
        Log.d("EmployeeScreens", "Token FCM configurado")
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CuentaPage", "Deteniendo listener de horarios")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Perfil del usuario con imagen
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Imagen de perfil
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFF2196F3), CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Avatar por defecto",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    // Botón para editar foto con progreso
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                            .clickable(enabled = !isUploadingImage) {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            CircularProgressIndicator(
                                progress = uploadProgress / 100f,
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "$uploadProgress%",
                                fontSize = 6.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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

            // Editar Cuenta - inputs siempre visibles
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                        enabled = (!isLoading),
                        isError = isEditingProfile && editableName.isBlank(),
                        supportingText = {
                            if (isEditingProfile && editableName.isBlank()) {
                                Text("El nombre es requerido", color = Color(0xFFEF4444))
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF2196F3)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editableEmail,
                                onValueChange = { editableEmail = it },
                                label = { Text("Correo") },
                                enabled = false,
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                    Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editablePhone,
                                onValueChange = { editablePhone = it },
                                label = { Text("Teléfono") },
                        enabled = (!isLoading),
                        isError = isEditingProfile && !isPhoneValid,
                        supportingText = {
                            if (isEditingProfile && !isPhoneValid) {
                                Text("Teléfono debe tener 7-15 dígitos", color = Color(0xFFEF4444))
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = Color(0xFF2196F3)
                            )
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

            Spacer(modifier = Modifier.height(8.dp))

            // Cambiar Contraseña - inputs siempre visibles
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seguridad",
                        fontSize = 14.sp,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                            },
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                            },
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                            },
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón de cerrar sesión en esquina superior derecha
        IconButton(
            onClick = {
                Log.d("CuentaPage", "Botón de logout presionado")
                authViewModel.logout()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Cerrar Sesión",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
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

// Función para actualizar la URL de la imagen en Firebase Database
private fun updateProfileImageUrl(
    imageUrl: String,
    viewModel: HomeViewModel,
    onShowAlert: (String, Color) -> Unit
) {
    try {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            onShowAlert("Usuario no autenticado", Color(0xFFEF4444))
            return
        }

        val userId = firebaseUser.uid
        val database = FirebaseDatabase.getInstance().getReference("User")

        database.child(userId)
            .child("photoURL")
            .setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("UpdateImage", "URL de imagen actualizada en Firebase")
                viewModel.loadCurrentUser()
                onShowAlert("Foto de perfil actualizada", Color(0xFF10B981))
            }
            .addOnFailureListener { e ->
                Log.e("UpdateImage", "Error actualizando URL", e)
                onShowAlert("Error actualizando foto: ${e.message}", Color(0xFFEF4444))
            }

    } catch (e: Exception) {
        Log.e("UpdateImage", "Error general", e)
        onShowAlert("Error inesperado: ${e.message}", Color(0xFFEF4444))
    }
}

// Función para actualizar datos del usuario
private fun updateUserData(
    name: String,
    email: String,
    phone: String,
    context: android.content.Context,
    viewModel: HomeViewModel,
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

// Función para actualizar contraseña
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
