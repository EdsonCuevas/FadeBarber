package com.example.fadebarber.ui.employee.pages

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.employee.components.CardAppointment
import com.example.fadebarber.ui.employee.components.CurrentAppointmentCard
import com.example.fadebarber.ui.employee.components.QRScannerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPage(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    var showCancelDialog by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var alertColor by remember { mutableStateOf(Color.Transparent) }
    var showAlert by remember { mutableStateOf(false) }

    // Estado para el escáner QR
    var showScanner by remember { mutableStateOf(false) }
    val scannerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val promotions by viewModel.promotions.collectAsState(initial = emptyList())

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = false)
    }

    // Local helper to update appointment status (avoids unresolved reference issues)
    fun updateAppointmentStatusLocal(
        appointmentId: String,
        newStatus: Int,
        context: android.content.Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("UpdateAppointment", "Actualizando cita $appointmentId a estado $newStatus")
        try {
            if (appointmentId.isBlank()) {
                onError("ID de cita inválido")
                return
            }
            val database = FirebaseDatabase.getInstance().getReference("Appointment")
            val updates: Map<String, Any> = mapOf(
                "statusAppointment" to newStatus
            )
            database.child(appointmentId)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("UpdateAppointment", "Estado actualizado correctamente")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateAppointment", "Error actualizando estado", e)
                    onError("Error al actualizar: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("UpdateAppointment", "Error general", e)
            onError("Error inesperado: ${e.message}")
        }
    }
    // === Lógica de próxima cita ===
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    val todayAppointments = appointments
        .filter {
            it.idEmployee == user.id &&
                    LocalDate.parse(it.dateAppointment) == today &&
                    it.statusAppointment == 1
        }
        .sortedBy { LocalTime.parse(it.timeAppointment, formatter) }

    val currentAppointment = appointments.find {
        it.idEmployee == user.id &&
                LocalDate.parse(it.dateAppointment) == today &&
                it.statusAppointment == 2 // En proceso
    }

    val nextAppointment = if (currentAppointment != null) {
        val currentTimeRef = LocalTime.parse(currentAppointment.timeAppointment, formatter)
        todayAppointments.firstOrNull {
            LocalTime.parse(it.timeAppointment, formatter).isAfter(currentTimeRef)
        }
    } else {
        todayAppointments.firstOrNull {
            LocalTime.parse(it.timeAppointment, formatter).isAfter(currentTime)
        }
    }

    val nextAppointmentText = when {
        nextAppointment != null -> {
            val client = users.find { it.id == nextAppointment.idClient }
            "${nextAppointment.timeAppointment} - ${client?.nameUser ?: "Cliente"}"
        }
        else -> "No hay más citas para hoy"
    }

    // === INTERFAZ ===
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            /** HEADER CON IMAGEN Y NOMBRE **/
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A),
                                Color(0xFF3B82F6)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, ${user.nameUser}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        /*Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(4) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_start),
                                    contentDescription = "Estrella llena",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Icon(
                            painter = painterResource(id = R.drawable.ic_start),
                            contentDescription = "Estrella vacía",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4.0/5",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        }*/
                    }

                    Box {
                        if (!user.photoURL.isNullOrEmpty()) {
                            AsyncImage(
                                model = user.photoURL,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.perfil)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.perfil),
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Próxima cita",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = nextAppointmentText,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_reloj),
                                contentDescription = "Reloj",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
                
                /** CARD PRINCIPAL DE CITA EN CURSO **/
                CurrentAppointmentCard(
                    appointments = appointments,
                    users = users,
                    selectedAppointment = selectedAppointment,
                    user = user,
                    scope = scope,
                    onAppointmentClick = {
                        selectedAppointment = it
                        scope.launch { sheetState.show() }
                    }
                )

                /** TITULO CITAS PENDIENTES **/
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "Citas Pendientes",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }

                /** LISTADO DE CITAS **/
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val userAppointments = appointments
                        .filter { it.statusAppointment == 1 && it.idEmployee == user.id }

                    val todayAppointmentsFiltered = userAppointments
                        .filter { appointment ->
                            val appointmentDate = LocalDate.parse(appointment.dateAppointment)
                            appointmentDate == today
                        }
                        .sortedBy {
                            LocalTime.parse(it.timeAppointment, formatter)
                        }

                    if (todayAppointmentsFiltered.isNotEmpty()) {
                        todayAppointmentsFiltered.forEach { appointment ->
                            CardAppointment(
                                appointment = appointment,
                                services = services,
                                users = users,
                                promotions = promotions,
                                onAppointmentClick = {
                                    selectedAppointment = it
                                    scope.launch { sheetState.show() }
                                }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_clock),
                            contentDescription = "Calendario",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "No hay citas disponibles",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                /** MODAL BOTTOM SHEET **/
                if (selectedAppointment != null) {
                    ModalBottomSheet(
                        sheetState = sheetState,
                        containerColor = Color.White,
                        onDismissRequest = { selectedAppointment = null }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            selectedAppointment?.let { appointment ->

                                // Header - Estado de la Cita
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Estado de la Cita",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val statusInfo = when (appointment.statusAppointment) {
                                        1 -> Pair("Pendiente", Color(0xFFF59E0B))
                                        2 -> Pair("En curso", Color(0xFF22C55E))
                                        4 -> Pair("Cancelada", Color(0xFFEF4444))
                                        else -> Pair("Desconocido", Color.Gray)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .background(
                                                statusInfo.second.copy(alpha = 0.1f),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(statusInfo.second, CircleShape)
                                        )
                                        Text(
                                            text = statusInfo.first,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = statusInfo.second
                                        )
                                    }
                                }

                                // Card Principal del Servicio
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        Color(0xFFE6F0FF),
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✂", fontSize = 24.sp)
                                            }

                                            Column {
                                                val titles = mutableListOf<String>()

                                                if (appointment.idPromotion.isNotEmpty()) {
                                                    val validPromotionIds =
                                                        appointment.idPromotion.filterNotNull()
                                                    if (validPromotionIds.isNotEmpty()) {
                                                        val firstPromotion = promotions.find {
                                                            it.id == validPromotionIds.firstOrNull()
                                                        }
                                                        if (firstPromotion != null) {
                                                            val totalPromotions =
                                                                validPromotionIds.size
                                                            titles.add(
                                                                if (totalPromotions > 1) {
                                                                    "${firstPromotion.namePromotion} + ${totalPromotions - 1} más"
                                                                } else {
                                                                    firstPromotion.namePromotion
                                                                        ?: "Promoción"
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                if (appointment.serviceId.isNotEmpty()) {
                                                    val validServiceIds =
                                                        appointment.serviceId.filterNotNull()
                                                    val selectedServices =
                                                        services.filter { it.id in validServiceIds }
                                                    if (selectedServices.isNotEmpty()) {
                                                        titles.add(
                                                            if (selectedServices.size > 1) {
                                                                "${selectedServices[0].nameService} + ${selectedServices.size - 1} más"
                                                            } else {
                                                                selectedServices[0].nameService
                                                                    ?: "Servicio"
                                                            }
                                                        )
                                                    }
                                                }

                                                if (titles.isNotEmpty()) {
                                                    Text(
                                                        text = titles[0],
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }

                                                val client =
                                                    users.find { it.id == appointment.idClient }
                                                Text(
                                                    text = "con ${client?.nameUser ?: "Cliente"}",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.DateRange,
                                                    contentDescription = "Fecha",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = appointment.dateAppointment.orEmpty(),
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AccessTime,
                                                    contentDescription = "Hora",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = appointment.timeAppointment ?: "0:00",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AttachMoney,
                                                    contentDescription = "Precio",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "${appointment.totalPrice ?: "0"}.00 ${appointment.methodPayment ?: ""}",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.HourglassBottom,
                                                    contentDescription = "Duración",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "${appointment.durationTotal ?: "0"} min",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Lista de Servicios
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Detalles del servicio",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )

                                        if (appointment.idPromotion.isNotEmpty()) {
                                            appointment.idPromotion.filterNotNull()
                                                .forEach { promotionId ->
                                                    val promotion =
                                                        promotions.find { it.id == promotionId }
                                                    promotion?.let { promo ->
                                                        Column(
                                                            verticalArrangement = Arrangement.spacedBy(
                                                                4.dp
                                                            )
                                                        ) {
                                                            Text(
                                                                text = "Promoción: ${promo.namePromotion}",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFF7C3AED),
                                                            )

                                                            promo.servicePromotion?.forEach { serviceId ->
                                                                val service =
                                                                    services.find { it.id == serviceId }
                                                                service?.let { srv ->
                                                                    Row(
                                                                        modifier = Modifier.padding(
                                                                            start = 12.dp
                                                                        ),
                                                                        verticalAlignment = Alignment.Top,
                                                                        horizontalArrangement = Arrangement.spacedBy(
                                                                            8.dp
                                                                        )
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(6.dp)
                                                                                .background(
                                                                                    Color(0xFF64748B),
                                                                                    CircleShape
                                                                                )
                                                                                .padding(top = 8.dp)
                                                                        )
                                                                        Column {
                                                                            Text(
                                                                                text = srv.nameService
                                                                                    ?: "Servicio",
                                                                                fontSize = 13.sp,
                                                                                fontWeight = FontWeight.Medium,
                                                                                color = Color(
                                                                                    0xFF374151
                                                                                )
                                                                            )
                                                                            if (!srv.descriptionService.isNullOrBlank()) {
                                                                                Text(
                                                                                    text = srv.descriptionService,
                                                                                    fontSize = 12.sp,
                                                                                    color = Color(
                                                                                        0xFF6B7280
                                                                                    )
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                        }

                                        if (appointment.serviceId.isNotEmpty()) {
                                            appointment.serviceId.filterNotNull()
                                                .forEach { serviceId ->
                                                    val service =
                                                        services.find { it.id == serviceId }
                                                    service?.let { srv ->
                                                        Row(
                                                            verticalAlignment = Alignment.Top,
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .background(
                                                                        Color(0xFF3B82F6),
                                                                        CircleShape
                                                                    )
                                                                    .padding(top = 8.dp)
                                                            )
                                                            Column {
                                                                Text(
                                                                    text = srv.nameService
                                                                        ?: "Servicio",
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = Color(0xFF374151)
                                                                )
                                                                if (!srv.descriptionService.isNullOrBlank()) {
                                                                    Text(
                                                                        text = srv.descriptionService,
                                                                        fontSize = 12.sp,
                                                                        color = Color(0xFF6B7280)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                        }

                                        if (appointment.serviceId.isEmpty() && appointment.idPromotion.isEmpty()) {
                                            Text(
                                                text = "No hay servicios especificados",
                                                fontSize = 13.sp,
                                                color = Color(0xFF6B7280),
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }

                                // Botones de Acción
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Mostrar botón Empezar o Finalizar según el estado
                                    when (appointment.statusAppointment) {
                                        1 -> { // Activo - Mostrar Empezar
                                            Button(
                                                onClick = {
                                                    // Validar si hay otra cita en proceso
                                                    val hasRunningAppointment = appointments.any {
                                                        it.idEmployee == user.id &&
                                                                it.statusAppointment == 2 &&
                                                                it.id != appointment.id
                                                    }

                                                    if (hasRunningAppointment) {
                                                        alertMessage =
                                                            "Ya hay una cita en proceso. Finalízala antes de comenzar otra."
                                                        alertColor = Color(0xFFEF4444)
                                                        showAlert = true
                                                    } else {
                                                        updateAppointmentStatusLocal(
                                                            appointmentId = appointment.id ?: "",
                                                            newStatus = 2,
                                                            context = context,
                                                            onSuccess = {
                                                                alertMessage =
                                                                    "Cita iniciada correctamente"
                                                                alertColor = Color(0xFF10B981)
                                                                showAlert = true
                                                                selectedAppointment = null
                                                                scope.launch { sheetState.hide() }
                                                            },
                                                            onError = { error ->
                                                                alertMessage = error
                                                                alertColor = Color(0xFFEF4444)
                                                                showAlert = true
                                                            }
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF3B82F6)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_user),
                                                        contentDescription = "Empezar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Empezar",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        2 -> { // En Proceso - Mostrar Finalizar
                                            Button(
                                                onClick = {
                                                    updateAppointmentStatusLocal(
                                                        appointmentId = appointment.id ?: "",
                                                        newStatus = 3, // Estado finalizado
                                                        context = context,
                                                        onSuccess = {
                                                            alertMessage =
                                                                "Cita finalizada correctamente"
                                                            alertColor = Color(0xFF10B981)
                                                            showAlert = true
                                                            selectedAppointment = null
                                                            scope.launch { sheetState.hide() }
                                                        },
                                                        onError = { error ->
                                                            alertMessage = error
                                                            alertColor = Color(0xFFEF4444)
                                                            showAlert = true
                                                        }
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF10B981)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_user),
                                                        contentDescription = "Finalizar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Finalizar Cita",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        4 -> { // Cancelada - Mostrar Reactivar
                                            Button(
                                                onClick = {
                                                    updateAppointmentStatusLocal(
                                                        appointmentId = appointment.id ?: "",
                                                        newStatus = 1, // Reactivar a estado activo
                                                        context = context,
                                                        onSuccess = {
                                                            alertMessage =
                                                                "Cita reactivada correctamente"
                                                            alertColor = Color(0xFF10B981)
                                                            showAlert = true
                                                            selectedAppointment = null
                                                            scope.launch { sheetState.hide() }
                                                        },
                                                        onError = { error ->
                                                            alertMessage = error
                                                            alertColor = Color(0xFFEF4444)
                                                            showAlert = true
                                                        }
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF3B82F6)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_user),
                                                        contentDescription = "Reactivar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = "Reactivar Cita",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Botón Cancelar (siempre visible excepto si ya está cancelada o finalizada)
                                    if (appointment.statusAppointment == 1) {
                                        OutlinedButton(
                                            onClick = {
                                                showCancelDialog = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_user),
                                                    contentDescription = "Cancelar",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "Cancelar Cita",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            // FAB de escaneo QR
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = { showScanner = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Escanear QR"
                    )
                }
            }

            // Modal para escanear QR
            if (showScanner) {
                ModalBottomSheet(
                    sheetState = scannerSheetState,
                    containerColor = Color.White,
                    onDismissRequest = { showScanner = false }
                ) {
                    QRScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onResult = { code ->
                            showScanner = false
                            alertMessage = "QR detectado: $code"
                            alertColor = Color(0xFF10B981)
                            showAlert = true
                        },
                        onClose = { showScanner = false }
                    )
                }
            }

            // Dialog de confirmación de cancelación
            if (showCancelDialog && selectedAppointment != null) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = {
                        Text(
                            text = "Cancelar Cita",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Text(
                            text = "¿Estás seguro de que deseas cancelar esta cita? Esta acción no se puede deshacer.",
                            fontSize = 16.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedAppointment?.let { appointment ->
                                    updateAppointmentStatusLocal(
                                        appointmentId = appointment.id ?: "",
                                        newStatus = 4,
                                        context = context,
                                        onSuccess = {
                                            alertMessage = "Cita cancelada correctamente"
                                            alertColor = Color(0xFF10B981)
                                            showAlert = true
                                            showCancelDialog = false
                                            selectedAppointment = null
                                            scope.launch { sheetState.hide() }
                                        },
                                        onError = { error ->
                                            alertMessage = error
                                            alertColor = Color(0xFFEF4444)
                                            showAlert = true
                                            showCancelDialog = false
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        ) {
                            Text("Sí, cancelar", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("No, mantener", color = Color(0xFF64748B))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Alerta de mensajes
            if (showAlert) {
                AlertDialog(
                    onDismissRequest = { showAlert = false },
                    title = {
                        Text(
                            text = if (alertColor == Color(0xFF10B981)) "Éxito" else "Error",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(text = alertMessage)
                    },
                    confirmButton = {
                        Button(
                            onClick = { showAlert = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = alertColor
                            )
                        ) {
                            Text("Aceptar", color = Color.White)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
