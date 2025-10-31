package com.example.fadebarber.ui.admin.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fadebarber.R
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.viewmodel.AdminDashboardViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEmployees() {
    val vm: AdminDashboardViewModel = viewModel()
    val barbers by vm.barbers.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val services by vm.services.collectAsState()
    val users by vm.users.collectAsState()

    LaunchedEffect(Unit) { vm.startListeners() }
    DisposableEffect(Unit) {
        onDispose { vm.stopListeners() }
    }

    var selectedFilter by remember { mutableStateOf("Hoy") }
    var selectedBarber by remember { mutableStateOf<Pair<UserData, BarberStats>?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Calcular estadísticas según el filtro seleccionado
    val filteredAppointments = remember(appointments, selectedDate, selectedFilter) {
        when (selectedFilter) {
            "Hoy" -> appointments.filter { vm.isToday(it.dateAppointment) }
            "Fecha Personalizada" -> {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateStr = dateFormat.format(selectedDate.time)
                appointments.filter { it.dateAppointment == selectedDateStr }
            }
            else -> appointments.filter { vm.isToday(it.dateAppointment) }
        }
    }

    // Estadísticas por barbero
    val barberStats = remember(barbers, filteredAppointments) {
        barbers.map { barber ->
            val barberAppointments = filteredAppointments.filter { it.idEmployee == barber.id }
            val completedCount = barberAppointments.count { it.statusAppointment == 4 }
            val totalIncome = barberAppointments
                .filter { it.statusAppointment == 4 }
                .sumOf { it.totalPrice ?: 0 }

            // Calcular rating promedio (simulado por ahora)
            val rating = 4.5 + (barber.nameUser.length % 5) / 10.0

            barber to BarberStats(
                citasHoy = barberAppointments.size,
                ingresos = totalIncome,
                rating = rating,
                appointments = barberAppointments
            )
        }.sortedByDescending { it.second.citasHoy }
    }

    val totalCitas = filteredAppointments.size
    val totalIngresos = filteredAppointments
        .filter { it.statusAppointment == 4 }
        .sumOf { it.totalPrice ?: 0 }
    val totalBarberos = barbers.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header Card
        HeaderCard(
            totalCitas = totalCitas,
            totalIngresos = totalIngresos,
            totalBarberos = totalBarberos
        )

        // Título y Filtro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Barberos Activos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // Filtro con calendario
            FilterChip(
                selected = true,
                onClick = { showCalendar = !showCalendar },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedFilter == "Hoy") "Hoy" else {
                                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                                val month = when (selectedDate.get(Calendar.MONTH)) {
                                    Calendar.JANUARY -> "Ene"
                                    Calendar.FEBRUARY -> "Feb"
                                    Calendar.MARCH -> "Mar"
                                    Calendar.APRIL -> "Abr"
                                    Calendar.MAY -> "May"
                                    Calendar.JUNE -> "Jun"
                                    Calendar.JULY -> "Jul"
                                    Calendar.AUGUST -> "Ago"
                                    Calendar.SEPTEMBER -> "Sep"
                                    Calendar.OCTOBER -> "Oct"
                                    Calendar.NOVEMBER -> "Nov"
                                    Calendar.DECEMBER -> "Dic"
                                    else -> ""
                                }
                                "$day $month"
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showCalendar) Icons.Default.ExpandLess else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2563EB),
                    selectedLabelColor = Color.White
                )
            )
        }

        // Calendario desplegable
        androidx.compose.animation.AnimatedVisibility(
            visible = showCalendar,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            InlineCalendar(
                selectedDate = selectedDate,
                onDateSelected = { calendar ->
                    selectedDate = calendar
                    val today = Calendar.getInstance()
                    selectedFilter = if (
                        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    ) {
                        "Hoy"
                    } else {
                        "Fecha Personalizada"
                    }
                    showCalendar = false
                },
                onTodaySelected = {
                    selectedDate = Calendar.getInstance()
                    selectedFilter = "Hoy"
                    showCalendar = false
                }
            )
        }

        // Lista de barberos
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(barberStats) { (barber, stats) ->
                BarberCard(
                    barber = barber,
                    stats = stats,
                    isOnline = vm.isUserOnline(barber.id),
                    onClick = { selectedBarber = barber to stats }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Bottom Sheet con detalles del barbero
    if (selectedBarber != null) {
        BarberDetailsBottomSheet(
            barber = selectedBarber!!.first,
            stats = selectedBarber!!.second,
            services = services,
            users = users,
            onDismiss = { selectedBarber = null }
        )
    }
}

@Composable
fun InlineCalendar(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onTodaySelected: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del mes con navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior",
                        tint = Color(0xFF2563EB)
                    )
                }

                Text(
                    text = "${getMonthName(currentMonth.get(Calendar.MONTH))} ${currentMonth.get(Calendar.YEAR)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )

                IconButton(onClick = {
                    currentMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente",
                        tint = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Días de la semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid del calendario
            val daysInMonth = getDaysInMonth(currentMonth)
            val firstDayOfWeek = getFirstDayOfWeek(currentMonth)
            val today = Calendar.getInstance()

            Column {
                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val currentDay = dayCounter
                                val isSelected = selectedDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                        selectedDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                        selectedDate.get(Calendar.DAY_OF_MONTH) == currentDay

                                val isToday = today.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                        today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                        today.get(Calendar.DAY_OF_MONTH) == currentDay

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF2563EB)
                                                isToday -> Color(0xFF2563EB).copy(alpha = 0.1f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            val newDate = (currentMonth.clone() as Calendar).apply {
                                                set(Calendar.DAY_OF_MONTH, currentDay)
                                            }
                                            onDateSelected(newDate)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentDay.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> Color(0xFF2563EB)
                                            else -> Color(0xFF1F2937)
                                        }
                                    )
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón "Hoy"
            Button(
                onClick = onTodaySelected,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB).copy(alpha = 0.1f),
                    contentColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ir a Hoy", fontWeight = FontWeight.Medium)
            }
        }
    }
}

fun getMonthName(month: Int): String {
    return when (month) {
        Calendar.JANUARY -> "Enero"
        Calendar.FEBRUARY -> "Febrero"
        Calendar.MARCH -> "Marzo"
        Calendar.APRIL -> "Abril"
        Calendar.MAY -> "Mayo"
        Calendar.JUNE -> "Junio"
        Calendar.JULY -> "Julio"
        Calendar.AUGUST -> "Agosto"
        Calendar.SEPTEMBER -> "Septiembre"
        Calendar.OCTOBER -> "Octubre"
        Calendar.NOVEMBER -> "Noviembre"
        Calendar.DECEMBER -> "Diciembre"
        else -> ""
    }
}

fun getDaysInMonth(calendar: Calendar): Int {
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun getFirstDayOfWeek(calendar: Calendar): Int {
    val firstDay = (calendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return firstDay.get(Calendar.DAY_OF_WEEK) - 1
}

data class BarberStats(
    val citasHoy: Int,
    val ingresos: Int,
    val rating: Double,
    val appointments: List<AppointmentClientData>
)

@Composable
fun HeaderCard(
    totalCitas: Int,
    totalIngresos: Int,
    totalBarberos: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        color = Color(0xFF2563EB),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                    )
                )
                .padding(20.dp)
        ) {
            // Header superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BarberPro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Panel Administrador",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.profilelogo),
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fecha
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = when (calendar.get(Calendar.MONTH)) {
                Calendar.NOVEMBER -> "Nov"
                Calendar.OCTOBER -> "Oct"
                Calendar.DECEMBER -> "Dic"
                else -> ""
            }
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lunes"
                Calendar.TUESDAY -> "Martes"
                Calendar.WEDNESDAY -> "Miércoles"
                Calendar.THURSDAY -> "Jueves"
                Calendar.FRIDAY -> "Viernes"
                Calendar.SATURDAY -> "Sábado"
                Calendar.SUNDAY -> "Domingo"
                else -> ""
            }

            Text(
                text = "Hoy",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "$dayOfWeek, $dayOfMonth $month",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = totalCitas.toString(),
                    label = "Citas"
                )
                StatItem(
                    value = "$$totalIngresos",
                    label = "Ingresos"
                )
                StatItem(
                    value = totalBarberos.toString(),
                    label = "Barberos"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit,
    onTodaySelected: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.timeInMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        onDateSelected(calendar)
                    }
                }
            ) {
                Text("Aceptar", color = Color(0xFF2563EB))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onTodaySelected) {
                    Text("Hoy", color = Color(0xFF2563EB))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = Color(0xFF6B7280))
                }
            }
        },
        text = {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF2563EB),
                    todayContentColor = Color(0xFF2563EB),
                    todayDateBorderColor = Color(0xFF2563EB)
                )
            )
        }
    )
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun BarberCard(
    barber: UserData,
    stats: BarberStats,
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when {
        isOnline -> Color(0xFF10B981)
        stats.citasHoy > 0 -> Color(0xFFF97316)
        else -> Color(0xFFEF4444)
    }

    val statusText = when {
        isOnline -> "Disponible"
        stats.citasHoy > 0 -> "En servicio"
        else -> "Ocupado"
    }

    // Próxima cita
    val nextAppointment = stats.appointments
        .filter { it.statusAppointment != 4 && it.statusAppointment != 5 }
        .minByOrNull { it.timeAppointment ?: "99:99" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto con indicador de estado
            Box {
                // Imagen de perfil
                if (barber.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = barber.photoURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF2196F3), CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profilelogo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = barber.nameUser,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (nextAppointment != null) {
                        Text(
                            text = " • Próxima: ${nextAppointment.timeAppointment}",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Flecha
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Estadísticas debajo
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MiniStatCard(
            value = stats.citasHoy.toString(),
            label = "Citas hoy"
        )
        MiniStatCard(
            value = "$${stats.ingresos}",
            label = "Ingresos"
        )
        MiniStatCard(
            value = String.format("%.1f", stats.rating),
            label = "Rating"
        )
    }
}

@Composable
fun MiniStatCard(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarberDetailsBottomSheet(
    barber: UserData,
    stats: BarberStats,
    services: List<ServiceData>,
    users: List<UserData>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (barber.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = barber.photoURL,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF2196F3), CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profilelogo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = barber.nameUser,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Barbero Profesional",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Estadísticas del día
            Text(
                text = "Estadísticas de Hoy",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    value = stats.citasHoy.toString(),
                    label = "Citas",
                    color = Color(0xFF2563EB)
                )
                StatCard(
                    value = "$${stats.ingresos}",
                    label = "Ingresos",
                    color = Color(0xFF10B981)
                )
                StatCard(
                    value = String.format("%.1f", stats.rating),
                    label = "Rating",
                    color = Color(0xFFF59E0B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Citas del día
            if (stats.appointments.isNotEmpty()) {
                Text(
                    text = "Citas del Día (${stats.appointments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(12.dp))

                stats.appointments.sortedBy { it.timeAppointment }.forEach { appointment ->
                    AppointmentRowItem(
                        appointment = appointment,
                        services = services,
                        users = users
                    )
                }
            } else {
                Text(
                    text = "Sin citas para hoy",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun AppointmentRowItem(
    appointment: AppointmentClientData,
    services: List<ServiceData>,
    users: List<UserData>
) {
    val client = users.find { it.id == appointment.idClient }
    val service = services.firstOrNull { it.id == appointment.serviceId?.firstOrNull() }

    val statusColor = when (appointment.statusAppointment) {
        1 -> Color(0xFFF97316)
        3 -> Color(0xFF3B82F6)
        4 -> Color(0xFF10B981)
        else -> Color(0xFF9CA3AF)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9FAFB)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora
            Text(
                text = appointment.timeAppointment ?: "00:00",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.width(60.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client?.nameUser ?: appointment.nameClient ?: "Cliente",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = service?.nameService ?: "Servicio",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Text(
                text = "$${appointment.totalPrice ?: 0}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
        }
    }
}