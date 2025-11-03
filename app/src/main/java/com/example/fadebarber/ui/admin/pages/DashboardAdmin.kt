package com.example.fadebarber.ui.admin.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.viewmodel.AdminDashboardViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdmin() {
    val vm: AdminDashboardViewModel = viewModel()
    val barbers by vm.barbers.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val services by vm.services.collectAsState()
    val users by vm.users.collectAsState()
    val readings by vm.readings.collectAsState()

    LaunchedEffect(Unit) { vm.startListeners() }
    DisposableEffect(Unit) {
        onDispose { vm.stopListeners() }
    }

    var selectedBarber by remember { mutableStateOf<Pair<UserData, BarberStats>?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))) }

    // Calcular estadísticas según el filtro seleccionado
    val filteredAppointments = remember(appointments, selectedDate) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(selectedDate.time)
        appointments.filter { it.dateAppointment == selectedDateStr }
    }

    // Verificar si es hoy
    val isToday = remember(selectedDate) {
        val today = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
        selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    // Filtrar barberos activos (última lectura del día es "entrada")
    val activeBarbersIds = remember(readings, selectedDate) {
        val activeIds = mutableSetOf<String>()
        barbers.forEach { barber ->
            val barberReadings = readings
                .filter { it.userId == barber.id }
                .filter { vm.isSameDay(it.timestamp, selectedDate) }
                .sortedByDescending { it.timestamp }

            // El barbero está activo si su última lectura del día es "entrada"
            if (barberReadings.firstOrNull()?.tipo == "entrada") {
                activeIds.add(barber.id)
            }
        }
        activeIds
    }

    // Estadísticas por barbero (TODOS los barberos)
    val barberStats = remember(barbers, filteredAppointments, activeBarbersIds) {
        barbers.map { barber ->
            val barberAppointments = filteredAppointments.filter { it.idEmployee == barber.id }
            val completedCount = barberAppointments.count { it.statusAppointment == 3 }
            val enServicio = barberAppointments.count { it.statusAppointment == 2 }
            val totalIncome = barberAppointments
                .filter { it.statusAppointment == 3 }
                .sumOf { it.totalPrice ?: 0 }

            barber to BarberStats(
                citasHoy = barberAppointments.size,
                enServicio = enServicio,
                ingresos = totalIncome,
                completed = completedCount,
                appointments = barberAppointments,
                isActive = activeBarbersIds.contains(barber.id)
            )
        }.sortedByDescending { it.second.citasHoy }
    }

    val totalCitas = filteredAppointments.size
    val totalIngresos = filteredAppointments
        .filter { it.statusAppointment == 4 }
        .sumOf { it.totalPrice ?: 0 }
    val totalBarberos = activeBarbersIds.size

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
            Column {
                Text(
                    text = "Todos los Barberos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "${barberStats.size} barberos totales • $totalBarberos activos",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Filtro con calendario
            FilterChip(
                selected = true,
                onClick = { showDatePicker = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isToday) "Hoy" else {
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
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2563EB),
                    selectedLabelColor = Color.White
                )
            )
        }

        // DatePicker Dialog
        if (showDatePicker) {
            // Convertir la fecha seleccionada a UTC para el DatePicker
            val selectedDateInUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateInUtc.timeInMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        // Convertir la fecha de hoy a UTC para comparar
                        val todayMexico = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
                        val todayUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            set(Calendar.YEAR, todayMexico.get(Calendar.YEAR))
                            set(Calendar.MONTH, todayMexico.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, todayMexico.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        return utcTimeMillis <= todayUtc.timeInMillis
                    }
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // Convertir de UTC a nuestra zona horaria
                                val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = millis
                                }

                                selectedDate = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City")).apply {
                                    set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                                    set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = "Seleccionar fecha",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    headline = null,
                    showModeToggle = false
                )
            }
        }

        // Lista de barberos
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (barberStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay barberos registrados",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            } else {
                items(barberStats) { (barber, stats) ->
                    BarberCard(
                        barber = barber,
                        stats = stats,
                        isOnline = vm.isUserOnline(barber.id),
                        onClick = { selectedBarber = barber to stats }
                    )
                }
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

data class BarberStats(
    val citasHoy: Int,
    val enServicio: Int,
    val completed: Int,
    val ingresos: Int,
    val appointments: List<AppointmentClientData>,
    val isActive: Boolean = false
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
                        text = "FadeBarber",
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
                    label = "Activos"
                )
            }
        }
    }
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
    // Determinar el color de estado basado en si está activo (registró entrada)
    val statusColor = when {
        !stats.isActive -> Color(0xFF9CA3AF) // Gris si no ha registrado entrada
        stats.enServicio > 0 -> Color(0xFFF97316) // Naranja si está ocupado
        else -> Color(0xFF10B981) // Verde si está disponible
    }

    val statusText = when {
        !stats.isActive -> "Sin registro"
        stats.enServicio > 0 -> "Ocupado"
        else -> "Disponible"
    }

    // Próxima cita
    val nextAppointment = stats.appointments
        .filter { it.statusAppointment == 1 && LocalTime.now() <= LocalTime.parse(it.timeAppointment) && it.dateAppointment == LocalDate.now().toString() }
        .minByOrNull { it.timeAppointment ?: "99:99" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Foto, Nombre, Estado, Próxima, Flecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto con indicador de estado
                Box {
                    if (barber.photoURL.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = barber.photoURL,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            error = painterResource(id = R.drawable.profilelogo)
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
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nombre y Estado
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = barber.nameUser,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (nextAppointment != null) {
                            Text(
                                text = " • Próxima: ${nextAppointment.timeAppointment}",
                                fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas en una sola fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    value = stats.citasHoy.toString(),
                    label = "Citas hoy"
                )
                StatColumn(
                    value = "$${stats.ingresos}",
                    label = "Ingresos"
                )
                StatColumn(
                    value = "${stats.completed}",
                    label = "Completadas"
                )
            }
        }
    }
}

@Composable
fun StatColumn(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Text(
            text = label,
            fontSize = 13.sp,
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
                Image(
                    painter = painterResource(id = R.drawable.profilelogo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
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
                    color = Color(0xFFF59E0B)
                )
                StatCard(
                    value = "$${stats.ingresos}",
                    label = "Ingresos",
                    color = Color(0xFF10B981)
                )
                StatCard(
                    value = "${stats.completed}",
                    label = "Completadas",
                    color = Color(0xFF2563EB)
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
        1 -> Color(0xFF3B82F6)
        2 -> Color(0xFFF97316)
        3 -> Color(0xFF10B981)
        4 -> Color(0xFFC71111)
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