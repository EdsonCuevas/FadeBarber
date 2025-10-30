package com.example.fadebarber.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.model.UserData
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    user: UserData,
    navController: NavHostController,
) {
    val info by viewModel.info.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()
    val context = LocalContext.current
    var showHoursDialog by remember { mutableStateOf(false) }

    // Iniciar listeners cuando se compone la pantalla
    LaunchedEffect(Unit) {
        viewModel.startRealtimeListeners()
        viewModel.listenToBarberInfo()
    }

    // Detener listeners cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRealtimeListeners()
            viewModel.stopListeningToBarberInfo()
        }
    }

    val scrollState = rememberScrollState()

    // Próxima cita del usuario (programada y futura)
    val today = LocalDate.now()
    val nowTime = LocalTime.now()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val localeEs = remember { Locale("es", "ES") }
    val dateFormatterEs = remember { DateTimeFormatter.ofPattern("EEEE, d MMM", localeEs) }

    val nextAppointment = remember(appointments, user.id) {
        appointments
            .filter { appt ->
                appt.idClient == user.id &&
                        appt.statusAppointment == 1 &&
                        !appt.dateAppointment.isNullOrBlank() &&
                        !appt.timeAppointment.isNullOrBlank()
            }
            .mapNotNull { appt ->
                val dateStr = appt.dateAppointment?.take(10)
                val date = try { dateStr?.let { LocalDate.parse(it) } } catch (_: Exception) { null }
                val time = try { appt.timeAppointment?.let { LocalTime.parse(it, timeFormatter) } } catch (_: Exception) { null }
                if (date != null && time != null) appt to (date to time) else null
            }
            .filter { (_, dt) ->
                val (date, time) = dt
                date.isAfter(today) || (date.isEqual(today) && time.isAfter(nowTime))
            }
            .sortedWith(compareBy({ it.second.first }, { it.second.second }))
            .firstOrNull()
            ?.first
    }

    val nextDateText = nextAppointment?.dateAppointment?.take(10)?.let {
        try {
            val d = LocalDate.parse(it)
            val formatted = d.format(dateFormatterEs)
            formatted.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(localeEs) else ch.toString() }
        } catch (_: Exception) { null }
    } ?: "Sin próximas citas"

    val nextTimeText = nextAppointment?.timeAppointment ?: ""
    val serviceNames = nextAppointment?.serviceId?.filterNotNull()?.mapNotNull { sid ->
        services.firstOrNull { it.id == sid }?.nameService
    } ?: emptyList()
    val servicesSummary = when {
        serviceNames.isEmpty() -> ""
        serviceNames.size == 1 -> serviceNames[0]
        else -> "${serviceNames[0]} + ${serviceNames.size - 1} más"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // Header con degradado azul marino y saludo dentro
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "FadeBarber",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tu Barbería de confianza",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hola ${user.nameUser}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¿Listo para tu próximo corte?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Botones de acción flotantes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .zIndex(2f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DateRange,
                            text = "Agendar",
                            onClick = { navController.navigate("agend") },
                            containerColor = Color.White,
                            textColor = Color(0xFF4B83FA),
                            iconTint = Color(0xFF4B83FA)
                        )

                        ActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Refresh,
                            text = "Mi Historial",
                            onClick = { navController.navigate("date") },
                            containerColor = Color.White,
                            textColor = Color(0xFF4B83FA),
                            iconTint = Color(0xFF4B83FA)
                        )
                    }
            }

            Column(
                modifier = Modifier.offset(y = (-25).dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            // Próxima Cita
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4B83FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Icono de reloj como fondo decorativo
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.20f),
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.BottomEnd)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Mi Cita",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = nextDateText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val detailsText = if (nextTimeText.isNotBlank() && servicesSummary.isNotBlank()) {
                            "$nextTimeText • $servicesSummary"
                        } else if (nextTimeText.isNotBlank()) {
                            nextTimeText
                        } else if (servicesSummary.isNotBlank()) {
                            servicesSummary
                        } else {
                            ""
                        }
                        if (detailsText.isNotBlank()) {
                            Text(
                                text = detailsText,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Ubicación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=Universidad+de+Colima+Campus+Naranjo")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFF2563EB).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Ubicación",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ubicación",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Universidad de Colima Campus Naranjo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Abrir en Maps",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            


            HorariosSection(info = info)
            }
        }

    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    containerColor: Color = Color.White,
    textColor: Color = Color(0xFF2196F3),
    iconTint: Color = Color(0xFF2196F3)
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InfoStatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2563EB).copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HorariosSection(info: BarberInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Horarios",
                        tint = Color(0xFF10B981)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Horarios de atención", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("Consulta disponibilidad por día", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (info == null) {
                /*Column {
                    repeat(3) {
                        SkeletonHorarioItem()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }*/
            } else {
                val diasOrdenados = listOf(
                    "monday" to "Lunes",
                    "tuesday" to "Martes",
                    "wednesday" to "Miércoles",
                    "thursday" to "Jueves",
                    "friday" to "Viernes",
                    "saturday" to "Sábado",
                    "sunday" to "Domingo"
                )
                diasOrdenados.forEach { (key, nombre) ->
                    val day = info.schedule[key]
                    HorarioLinea(
                        diaNombre = nombre,
                        start = day?.start,
                        end = day?.end,
                        abierto = day?.available == true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}


@Composable
fun HorarioLinea(
    diaNombre: String,
    start: String?,
    end: String?,
    abierto: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (abierto) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = diaNombre.first().toString(),
                color = if (abierto) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = diaNombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            if (abierto) {
                Text(text = "${start ?: "--:--"} a ${end ?: "--:--"}", fontSize = 13.sp, color = Color(0xFF334155))
            } else {
                Text(text = "Cerrado", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
        Box(
            modifier = Modifier
                .background(
                    if (abierto) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (abierto) "Abierto" else "Cerrado",
                fontSize = 12.sp,
                color = if (abierto) Color(0xFF065F46) else Color(0xFF7F1D1D)
            )
        }
    }
}