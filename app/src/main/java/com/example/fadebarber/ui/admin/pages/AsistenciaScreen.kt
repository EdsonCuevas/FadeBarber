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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fadebarber.R
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AsistenciaScreen() {
    val vm: AttendanceViewModel = viewModel()
    val barbers by vm.barbers.collectAsState()
    val readings by vm.readings.collectAsState()

    var selectedBarber by remember { mutableStateOf<AttendanceStatus?>(null) }

    LaunchedEffect(Unit) { vm.startListeners() }
    DisposableEffect(Unit) {
        onDispose { vm.stopListeners() }
    }

    // Agrupar lecturas por usuario para obtener el último estado
    val userStatusMap = remember(readings, barbers) {
        val statusMap = mutableMapOf<String, AttendanceStatus>()

        // Obtener la última lectura de cada usuario hoy
        readings
            .filter { vm.isToday(it.timestamp) }
            .groupBy { it.userId }
            .forEach { (userId, userReadings) ->
                val lastReading = userReadings.maxByOrNull { it.timestamp }
                if (lastReading != null) {
                    val barber = barbers.find { it.id == userId }
                    if (barber != null) {
                        statusMap[userId] = AttendanceStatus(
                            barber = barber,
                            isInside = lastReading.tipo == "entrada",
                            lastTime = lastReading.timestamp,
                            allReadings = userReadings.sortedByDescending { it.timestamp }
                        )
                    }
                }
            }
        statusMap
    }

    // Calcular estadísticas
    val activeBarbersCount = userStatusMap.count { it.value.isInside }

    // Calcular horas trabajadas hoy
    val totalWorkHours = remember(readings) {
        readings
            .filter { vm.isToday(it.timestamp) }
            .groupBy { it.userId }
            .map { (_, userReadings) ->
                calculateWorkHours(userReadings.sortedBy { it.timestamp })
            }
            .sum()
    }

    // Crear lista de registros ordenados por hora
    val attendanceRecords = remember(userStatusMap) {
        userStatusMap.values
            .sortedByDescending { it.lastTime }
            .toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        AttendanceHeader()

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                value = activeBarbersCount.toString(),
                label = "Barberos Activos",
                iconColor = Color(0xFF10B981),
                backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                value = "${String.format("%.1f", totalWorkHours)}h",
                label = "Horas Trabajadas",
                iconColor = Color(0xFF2563EB),
                backgroundColor = Color(0xFF2563EB).copy(alpha = 0.1f)
            )
        }

        // Registro de Asistencia Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro de Asistencia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        }

        // Lista de registros
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (attendanceRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros de asistencia hoy",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            } else {
                items(attendanceRecords) { status ->
                    AttendanceCard(
                        status = status,
                        onClick = { selectedBarber = status }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Bottom Sheet con historial de asistencia
    if (selectedBarber != null) {
        AttendanceHistoryBottomSheet(
            status = selectedBarber!!,
            onDismiss = { selectedBarber = null }
        )
    }
}

data class ReadingData(
    val timestamp: String = "",
    val tipo: String = "",
    val uid: String = "",
    val userId: String = ""
)

data class AttendanceStatus(
    val barber: UserData,
    val isInside: Boolean,
    val lastTime: String,
    val allReadings: List<ReadingData>
)

// Función para calcular horas trabajadas
fun calculateWorkHours(readings: List<ReadingData>): Double {
    var totalHours = 0.0
    var lastEntryTime: Date? = null
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Mexico_City")


    readings.forEach { reading ->
        try {
            val currentTime = dateFormat.parse(reading.timestamp)

            when (reading.tipo) {
                "entrada" -> {
                    lastEntryTime = currentTime
                }
                "salida" -> {
                    if (lastEntryTime != null && currentTime != null) {
                        val diffInMillis = currentTime.time - lastEntryTime!!.time
                        totalHours += diffInMillis / (1000.0 * 60 * 60) // Convertir a horas
                        lastEntryTime = null
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar error de parsing
        }
    }

    return totalHours
}

@Composable
fun AttendanceHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2563EB),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FadeBarber",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Panel Administrador",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryBottomSheet(
    status: AttendanceStatus,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    val isoFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    isoFormat.timeZone = TimeZone.getTimeZone("America/Mexico_City")

    // Calcular horas trabajadas
    val workHours = calculateWorkHours(status.allReadings)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header con foto y nombre
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (status.barber.photoURL.isNotEmpty()) {
                        AsyncImage(
                            model = status.barber.photoURL,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
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
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = status.barber.nameUser,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (status.isInside) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (status.isInside) "Actualmente Dentro" else "Actualmente Fuera",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (status.isInside) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Estadísticas del día
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticCard(
                        title = "Registros",
                        value = status.allReadings.size.toString(),
                        icon = Icons.Default.List,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatisticCard(
                        title = "Horas Trabajadas",
                        value = String.format("%.1fh", workHours),
                        icon = Icons.Default.Schedule,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Título del historial
            item {
                Text(
                    text = "Historial de Hoy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lista de registros
            items(status.allReadings.size) { index ->
                val reading = status.allReadings[index]
                val isEntry = reading.tipo == "entrada"

                val time = try {
                    val date = isoFormat.parse(reading.timestamp)
                    timeFormat.format(date ?: Date())
                } catch (e: Exception) {
                    "00:00"
                }

                val fullDate = try {
                    val date = isoFormat.parse(reading.timestamp)
                    dateFormat.format(date ?: Date())
                } catch (e: Exception) {
                    ""
                }

                HistoryItem(
                    time = time,
                    date = fullDate,
                    type = reading.tipo,
                    isEntry = isEntry,
                    showConnector = index < status.allReadings.size - 1
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Botón cerrar
            item {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun HistoryItem(
    time: String,
    date: String,
    type: String,
    isEntry: Boolean,
    showConnector: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isEntry) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isEntry) Icons.Default.Login else Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }

            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp)
        ) {
            Text(
                text = if (isEntry) "Entrada" else "Salida",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = date,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }

        // Hora
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isEntry) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
        ) {
            Text(
                text = time,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEntry) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    backgroundColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = backgroundColor
                ) {
                    Text(
                        text = "Hoy",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = iconColor
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceCard(
    status: AttendanceStatus,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isoFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    isoFormat.timeZone = TimeZone.getTimeZone("America/Mexico_City")


    val formattedTime = try {
        val date = isoFormat.parse(status.lastTime)
        timeFormat.format(date ?: Date())
    } catch (e: Exception) {
        "00:00"
    }

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
            // Foto del barbero
            if (status.barber.photoURL.isNotEmpty()) {
                AsyncImage(
                    model = status.barber.photoURL,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.profilelogo)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profilelogo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.barber.nameUser,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (status.isInside) Icons.Default.Login else Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (status.isInside) "Entrada: $formattedTime" else "Salida: $formattedTime",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Badge de estado
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (status.isInside) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (status.isInside) "Dentro" else "Fuera",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (status.isInside) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flecha
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}