package com.example.fadebarber.ui.client.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.components.QrAppointmentModal
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaPageClient(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: DashboardViewModel = viewModel()
) {

    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val promotions by viewModel.promotions.collectAsState(initial = emptyList())

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = Color(0xFF1E3A8A), darkIcons = false)
    }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Helper para actualizar estado de la cita (cancelar = 4)
    fun updateAppointmentStatusClient(
        appointmentId: String,
        newStatus: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (appointmentId.isBlank()) {
                onError("ID de cita inválido")
                return
            }
            val updates: Map<String, Any> = mapOf(
                "statusAppointment" to newStatus
            )
            com.google.firebase.database.FirebaseDatabase
                .getInstance()
                .getReference("Appointment")
                .child(appointmentId)
                .updateChildren(updates)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError("Error al actualizar: ${e.message}") }
        } catch (e: Exception) {
            onError("Error inesperado: ${e.message}")
        }
    }

    val filteredAppointments = appointments
        .filter { appointment ->
            val appointmentDate = appointment.dateAppointment?.substring(0, 10)
            appointment.idClient == user.id && // Usuario actual es el cliente
                    LocalDate.parse(appointmentDate, formatter) == selectedDate
        }
        .sortedBy {
            LocalTime.parse(it.timeAppointment, DateTimeFormatter.ofPattern("HH:mm"))
        }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // HEADER MEJORADO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reloj),
                            contentDescription = "Mis Citas",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Mis Citas",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Hola, ${user.nameUser}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // SELECTOR DE FECHAS CON NUEVO DISEÑO
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
        ) {
            items(days) { day ->
                val isSelected = selectedDate == day
                val isToday = day == today
                // Compute if the user has pending appointments on this day
                val hasPendingAppointments = appointments.any { appointment ->
                    val dateStr = appointment.dateAppointment?.substring(0, 10)
                    val apptDate = dateStr?.let { LocalDate.parse(it, formatter) }
                    appointment.idClient == user.id &&
                            apptDate == day &&
                            appointment.statusAppointment == 1
                }

                Card(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { selectedDate = day },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> Color(0xFF2563EB)
                            else -> Color.White
                        }
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
                                .uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isSelected -> Color.White.copy(alpha = 0.9f)
                                else -> Color(0xFF94A3B8)
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                else -> Color(0xFF1E293B)
                            }
                        )
                        // Indicador de citas pendientes o hoy (placeholder fijo para igualar alturas)
                        val dotColor = when {
                            hasPendingAppointments -> Color(0xFFF59E0B)
                            isToday && !isSelected -> Color(0xFF2563EB)
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .background(
                                    color = dotColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // HEADER LISTA DE CITAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedDate == today) "Hoy" else selectedDate.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es"))
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "${filteredAppointments.size} ${if (filteredAppointments.size == 1) "cita" else "citas"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LISTA DE CITAS CON NUEVO DISEÑO
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredAppointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Color(0xFF2563EB).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar_clock),
                                    contentDescription = "Sin citas",
                                    tint = Color(0xFF2563EB).copy(alpha = 0.6f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "No hay citas programadas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selecciona otra fecha para ver tus citas",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredAppointments) { appointment ->
                    ImprovedAppointmentCard(
                        appointment = appointment,
                        services = services,
                        users = users,
                        promotions = promotions,
                        onClick = { selectedAppointment = it }
                    )
                }
            }

        }
        if (selectedAppointment != null) {
            QrAppointmentModal(
                appointment = selectedAppointment!!,
                onDismiss = { selectedAppointment = null },
                onCancelAppointment = {
                    val id = selectedAppointment?.id ?: ""
                    if (id.isNotBlank()) {
                        updateAppointmentStatusClient(
                            appointmentId = id,
                            newStatus = 4,
                            onSuccess = {
                                // Opcional: podríamos actualizar UI local, pero el flujo debe refrescarse desde ViewModel
                            },
                            onError = { /* TODO: mostrar error si se desea */ }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ImprovedAppointmentCard(
    appointment: AppointmentClientData,
    services: List<ServiceData>,
    users: List<UserData>,
    promotions: List<PromotionData>,
    onClick: (AppointmentClientData) -> Unit
) {
    val statusColor = when (appointment.statusAppointment) {
        1 -> Color(0xFFF59E0B) // Pendiente - Verde
        2 -> Color(0xFF10B981) // En curso - Amarillo
        3 -> Color(0xFF2563EB) // Completada - Azul
        0 -> Color(0xFFEF4444) // Cancelada - Rojo
        else -> Color(0xFF6B7280)
    }
    val statusText = when (appointment.statusAppointment) {
        1 -> "Pendiente"
        2 -> "En curso"
        3 -> "Completada"
        4 -> "Cancelada"
        else -> "Desconocido"
    }

    val barbero = users.find { it.id == appointment.idEmployee }

    val servicesTitles = mutableListOf<String>()

    // Debug: Imprimir valores
    println("DEBUG - Appointment ID: ${appointment.id}")
    println("DEBUG - idPromotion: ${appointment.idPromotion}")
    println("DEBUG - serviceId: ${appointment.serviceId}")

    // Promociones
    appointment.idPromotion?.forEach { promoId ->
        promotions.find { it.id == promoId }?.let { promo ->
            servicesTitles.add("🎁 ${promo.namePromotion}")
        }
    }

    // Servicios
    appointment.serviceId?.forEach { serviceId ->
        services.find { it.id == serviceId }?.let { service ->
            servicesTitles.add(service.nameService ?: "Servicio")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(appointment) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // HEADER: Hora y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Hora",
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appointment.timeAppointment ?: "00:00",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Hora de cita",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DIVIDER
            Divider(
                color = Color(0xFFE2E8F0),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BARBERO
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF2563EB).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = barbero?.nameUser?.firstOrNull()?.uppercase() ?: "B",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Barbero",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = barbero?.nameUser ?: "Asignado",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SERVICIOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Servicios",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (servicesTitles.isNotEmpty()) {
                    servicesTitles.forEachIndexed { index, title ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Sin servicios asignados",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}