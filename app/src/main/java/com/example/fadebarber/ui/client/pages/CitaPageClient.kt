package com.example.fadebarber.ui.client.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.components.QrAppointmentModal
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaPageClient(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: HomeViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()
    val barbers by viewModel.barbers.collectAsState()
    val promotions by viewModel.promotions.collectAsState()

    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = Color(0xFF1E3A8A), darkIcons = false)
    }

    LaunchedEffect(Unit) {
        Log.d("CitaPageClient", "Iniciando listeners")
        viewModel.startRealtimeListeners()
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CitaPageClient", "Deteniendo listeners")
            viewModel.stopRealtimeListeners()
        }
    }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }
    val enabledDates = days.toSet()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val scrollState = rememberScrollState()

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

    val filteredAppointments = remember(appointments, user.id, selectedDate) {
        appointments
            .filter { appointment ->
                val appointmentDate = appointment.dateAppointment?.substring(0, 10)
                appointment.idClient == user.id &&
                        appointmentDate?.let { LocalDate.parse(it, formatter) } == selectedDate
            }
            .sortedBy {
                try {
                    LocalTime.parse(it.timeAppointment, DateTimeFormatter.ofPattern("HH:mm"))
                } catch (e: Exception) {
                    LocalTime.MIN
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
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

        // SELECTOR DE FECHAS CON HORIZONTAL SCROLL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            days.forEach { day ->
                val isSelected = selectedDate == day
                val isToday = day == today
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

        // Calendario mensual siempre visible (fijo)
        MonthCalendar(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            appointments = appointments,
            currentUserId = user.id,
            enabledDates = enabledDates,
            onMonthChange = { currentMonth = it },
            onSelectDate = { selectedDate = it }
        )

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

        // LISTA DE CITAS CON COLUMN EN LUGAR DE LAZYCOLUMN
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredAppointments.isEmpty()) {
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
            } else {
                filteredAppointments.forEach { appointment ->
                    ImprovedAppointmentCard(
                        appointment = appointment,
                        services = services,
                        users = barbers,
                        promotions = promotions,
                        onClick = { selectedAppointment = it }
                    )
                }
            }
        }

        // Espaciado final para mejor scroll
        Spacer(modifier = Modifier.height(24.dp))

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
                                selectedAppointment = null
                            },
                            onError = { /* TODO: mostrar error */ }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun MonthCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointments: List<AppointmentClientData>,
    currentUserId: String?,
    enabledDates: Set<LocalDate>,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val locale = Locale("es", "ES")
    val monthTitle = currentMonth.month
        .getDisplayName(java.time.format.TextStyle.FULL, locale)
        .replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString() } + " " + currentMonth.year

    val appointmentDates = remember(appointments, currentUserId) {
        appointments
            .filter { it.idClient == currentUserId }
            .mapNotNull { it.dateAppointment?.take(10) }
            .toSet()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Mes anterior",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_next),
                            contentDescription = "Mes siguiente",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                appointmentDates = appointmentDates,
                enabledDates = enabledDates,
                onDateClick = onSelectDate
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointmentDates: Set<String>,
    enabledDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val cells = mutableListOf<LocalDate?>().apply {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) {
            add(currentMonth.atDay(day))
        }
        while (size % 7 != 0) add(null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (rowStart in cells.indices step 7) {
            if (rowStart >= cells.size) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 7) {
                    val date = cells.getOrNull(rowStart + i)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            hasAppointments = date?.toString() in appointmentDates,
                            enabled = date != null && enabledDates.contains(date),
                            onClick = { date?.let { onDateClick(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    hasAppointments: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    if (date == null) {
        Box(modifier = Modifier.size(44.dp))
        return
    }

    val today = LocalDate.now()
    val isToday = date == today

    val bg = when {
        isSelected -> Color(0xFF2563EB)
        isToday -> Color(0xFFE3F2FD)
        else -> Color.Transparent
    }

    val fg = when {
        !enabled -> Color(0xFF94A3B8)
        isSelected -> Color.White
        isToday -> Color(0xFF2563EB)
        else -> Color(0xFF1E293B)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 15.sp,
                color = fg,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (enabled && hasAppointments && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                )
            }
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
        1 -> Color(0xFFF59E0B)
        2 -> Color(0xFF10B981)
        3 -> Color(0xFF2563EB)
        4 -> Color(0xFFEF4444)
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

    appointment.idPromotion?.forEach { promoId ->
        promotions.find { it.id == promoId }?.let { promo ->
            servicesTitles.add("🎁 ${promo.namePromotion}")
        }
    }

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
            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2563EB).copy(alpha = 0.1f), CircleShape),
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
                    servicesTitles.forEach { title ->
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