package com.example.fadebarber.ui.admin.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen() {
    val vm: AdminDashboardViewModel = viewModel()
    val barbers by vm.barbers.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val services by vm.services.collectAsState()
    val users by vm.users.collectAsState()

    LaunchedEffect(Unit) { vm.startListeners() }
    DisposableEffect(Unit) {
        onDispose { vm.stopListeners() }
    }

    // Calcular estadísticas
    val todayAppointments = remember(appointments) {
        appointments.filter { vm.isToday(it.dateAppointment) }
    }

    val completedCount = todayAppointments.count { it.statusAppointment == 4 }
    val inProgressCount = todayAppointments.count { it.statusAppointment == 3 }
    val pendingCount = todayAppointments.count { it.statusAppointment == 1 }

    // Barberos activos
    val activeBarbers = remember(todayAppointments, barbers) {
        barbers.filter { barber ->
            todayAppointments.any { it.idEmployee == barber.id }
        }.map { barber ->
            val apptCount = todayAppointments.count { it.idEmployee == barber.id }
            barber to apptCount
        }.sortedByDescending { it.second }
    }

    // Próximas citas
    val upcomingAppointments = remember(todayAppointments) {
        todayAppointments
            .filter { it.statusAppointment != 4 && it.statusAppointment != 5 }
            .sortedBy { it.timeAppointment }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BarberPro Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Panel de Control",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con fecha y total de citas
            item {
                Column {
                    val calendar = Calendar.getInstance()
                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = when (calendar.get(Calendar.MONTH)) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Hoy, $dayOfMonth $month",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = dayOfWeek,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        // Total de citas
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = todayAppointments.size.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                            Text(
                                text = "Citas",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            // Estadísticas (Completadas, En Progreso, Pendiente)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        count = completedCount,
                        label = "Completadas",
                        color = Color(0xFF10B981)
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        count = inProgressCount,
                        label = "En Progreso",
                        color = Color(0xFF3B82F6)
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        count = pendingCount,
                        label = "Pendiente",
                        color = Color(0xFFF97316)
                    )
                }
            }

            // Barberos Activos Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Barberos Activos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    TextButton(onClick = { /* Ver todos */ }) {
                        Text(
                            text = "Ver Todos",
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // Lista de barberos activos
            items(activeBarbers) { (barber, count) ->
                BarberActiveItem(
                    barber = barber,
                    appointmentCount = count,
                    isOnline = vm.isUserOnline(barber.id)
                )
            }

            // Próximas Citas Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximas Citas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    TextButton(onClick = { /* Ver agenda */ }) {
                        Text(
                            text = "Ver Agenda",
                            fontSize = 14.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }

            // Lista de próximas citas
            items(upcomingAppointments) { appointment ->
                UpcomingAppointmentItem(
                    appointment = appointment,
                    barbers = barbers,
                    services = services,
                    users = users
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 28.sp,
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

@Composable
fun BarberActiveItem(
    barber: UserData,
    appointmentCount: Int,
    isOnline: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil con indicador de estado
            Box {
                Image(
                    painter = painterResource(id = R.drawable.profilelogo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isOnline -> Color(0xFF10B981)
                                else -> Color(0xFFEF4444)
                            }
                        )
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y estado
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = barber.nameUser,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isOnline -> Color(0xFF10B981)
                                    appointmentCount > 0 -> Color(0xFF3B82F6)
                                    else -> Color(0xFFEF4444)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isOnline -> "Disponible"
                            appointmentCount > 0 -> "En Servicio"
                            else -> "Ocupado"
                        },
                        fontSize = 13.sp,
                        color = when {
                            isOnline -> Color(0xFF10B981)
                            appointmentCount > 0 -> Color(0xFF3B82F6)
                            else -> Color(0xFFEF4444)
                        }
                    )
                }
            }

            // Contador de citas
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = appointmentCount.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
                Text(
                    text = "Citas hoy",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun UpcomingAppointmentItem(
    appointment: AppointmentClientData,
    barbers: List<UserData>,
    services: List<ServiceData>,
    users: List<UserData>
) {
    val barber = barbers.find { it.id == appointment.idEmployee }
    val client = users.find { it.id == appointment.idClient }
    val service = services.firstOrNull { it.id == appointment.serviceId?.firstOrNull() }

    val statusColor = when (appointment.statusAppointment) {
        1 -> Color(0xFFF97316) // Pendiente
        2 -> Color(0xFF8B5CF6) // Confirmada
        3 -> Color(0xFF3B82F6) // En progreso
        else -> Color(0xFF9CA3AF)
    }

    val statusText = when (appointment.statusAppointment) {
        1 -> "Pendiente"
        2 -> "Confirmada"
        3 -> "En Progreso"
        else -> "Desconocido"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hora con indicador de color
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appointment.timeAppointment ?: "00:00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del cliente
            Text(
                text = client?.nameUser ?: appointment.nameClient ?: "Cliente",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Servicio y barbero
            Text(
                text = "${service?.nameService ?: "Servicio"} • ${barber?.nameUser ?: "Barbero"}",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Ver Detalles
            Button(
                onClick = { /* Ver detalles */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "Ver Detalles",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}