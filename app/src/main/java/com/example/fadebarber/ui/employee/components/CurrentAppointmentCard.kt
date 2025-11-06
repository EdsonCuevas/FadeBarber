package com.example.fadebarber.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.UserData
import kotlinx.coroutines.CoroutineScope
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import java.util.Locale

@Composable
fun CurrentAppointmentCard(
    appointments: List<AppointmentClientData>?,
    users: List<UserData>?,
    selectedAppointment: AppointmentClientData?,
    user: UserData,
    scope: CoroutineScope,
    onAppointmentClick: (AppointmentClientData?) -> Unit
) {
    val ongoingAppointment = appointments
        ?.firstOrNull { it.idEmployee == user.id && it.statusAppointment == 2 }

    // Hacemos clicable todo el Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onAppointmentClick(ongoingAppointment) },
        verticalAlignment = Alignment.Top
    ) {
        if (ongoingAppointment != null) {
            // Cita en curso
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header: "Cita en curso" + estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cita En Curso",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BlinkingDot(size = 10.dp, color = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "En curso",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Desplegar",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cliente + hora
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar del cliente
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF3B82F6), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = "Cliente",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Nombre y horario
                        Column {
                            val clientName = if (ongoingAppointment.idClient.isNullOrBlank()) {
                                ongoingAppointment.nameClient ?: "Cliente"
                            } else {
                                users
                                    ?.firstOrNull { it.id == ongoingAppointment.idClient }
                                    ?.nameUser
                                    ?: ongoingAppointment.nameClient
                                    ?: "Cliente"
                            }

                            Text(
                                text = clientName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )

                            // Rango de horario basado en slots de 30 min y duración total
                            val startTimeStr = ongoingAppointment.timeAppointment ?: ""
                            val durationMinutes = ongoingAppointment.durationTotal ?: 0
                            val parseFormatter = DateTimeFormatter.ofPattern("HH:mm")
                            val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.getDefault())
                            val startTime = try {
                                LocalTime.parse(startTimeStr, parseFormatter)
                            } catch (e: Exception) {
                                null
                            }
                            val slots = if (durationMinutes > 0) ceil(durationMinutes / 30.0).toInt() else 1
                            val endTime = startTime?.plusMinutes((slots * 30).toLong())
                            val startDisplay = startTime?.format(displayFormatter) ?: ""
                            val endDisplay = endTime?.format(displayFormatter) ?: ""

                            Text(
                                text = if (startDisplay.isNotBlank()) "$startDisplay - $endDisplay" else "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        } else {
            // No hay citas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No hay citas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF9CA3AF), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sin actividad",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No tienes citas programadas en este momento",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
