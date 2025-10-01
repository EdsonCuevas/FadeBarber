package com.example.fadebarber.ui.employee.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaPage(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

    // Filtrar citas por fecha seleccionada
    val filteredAppointments = appointments?.filter { appointment ->
        // Aquí deberías comparar con la fecha de la cita
        // appointment.date == selectedDate
        true // Temporal
    } ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // HEADER CON TÍTULO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reloj),
                            contentDescription = "Calendario",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Citas de Barbería",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es")))
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es")) else it.toString() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selecciona una fecha",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SELECTOR DE FECHAS - SOLUCIONADO
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(days) { day ->
                val isSelected = selectedDate == day
                val isToday = day == today

                // Usando Card normal en lugar de ElevatedCard
                Card(
                    modifier = Modifier
                        .width(60.dp)
                        .clickable { selectedDate = day },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> Color(0xFF1E3A8A)
                            isToday -> Color(0xFFEBF8FF)
                            else -> Color.White
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 6.dp else 2.dp
                    )
                )  {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayOfWeek.name.take(3).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isSelected -> Color.White
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF6B7280)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF1F2937)
                            }
                        )
                        Text(
                            text = day.month.name.take(3).uppercase(),
                            fontSize = 10.sp,
                            color = when {
                                isSelected -> Color.White.copy(alpha = 0.8f)
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF9CA3AF)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HEADER CITAS DEL DÍA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Citas De Hoy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = "${filteredAppointments.size} citas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LISTA DE CITAS
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredAppointments.ifEmpty {
                // Datos de ejemplo si no hay citas reales
                listOf(
                    SampleAppointment("09:00 AM", "Carlos Rodríguez", "Corte + Barba"),
                    SampleAppointment("10:30 AM", "Miguel García", "Corte Clásico"),
                    SampleAppointment("12:00 PM", "Luis Fernández", "Afeitado"),
                    SampleAppointment("02:30 PM", "Ana Martínez", "Corte Degradado"),
                    SampleAppointment("04:00 PM", "José López", "Corte + Lavado")
                )
            }) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    services = services,
                    users = users,
                    onClick = { }
                )
            }
        }
    }
}

// Clase para datos de ejemplo
data class SampleAppointment(
    val time: String,
    val clientName: String,
    val service: String,
)

@Composable
fun AppointmentCard(
    appointment: Any, // Puede ser AppointmentClientData o SampleAppointment
    services: List<Any>?,
    users: List<Any>?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color y hora
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                /*val (time, color) = when (appointment) {
                    is SampleAppointment -> appointment.time to appointment.color
                    else -> "00:00 AM" to Color(0xFF3B82F6)
                }*/

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val (time, clientName, service) = when (appointment) {
                    is SampleAppointment -> Triple(appointment.time, appointment.clientName, appointment.service)
                    else -> Triple("00:00 AM", "Cliente", "Servicio")
                }

                Text(
                    text = time,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )

                Text(
                    text = clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = service,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Iconos de acción
            Row {
                IconButton(
                    onClick = { /* Llamar cliente */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reloj),
                        contentDescription = "Llamar",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { /* Más opciones */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reloj),
                        contentDescription = "Más",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}