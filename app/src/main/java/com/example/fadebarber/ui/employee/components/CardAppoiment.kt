package com.example.fadebarber.ui.employee.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData

@Composable
fun CardAppointment(
    appointment: AppointmentClientData,
    services: List<ServiceData>,
    users: List<UserData>,
    promotions: List<PromotionData>,
    onAppointmentClick: (AppointmentClientData) -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        onClick = { onAppointmentClick(appointment) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // IZQUIERDA -> ICONO + INFO
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Círculo con ícono
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFF22D3EE),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_reloj),
                        contentDescription = "Hora",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Servicio + Cliente
                Column {
                    // Nombre servicios o promociones
                    if (!appointment.idPromotion.isNullOrEmpty()) {
                        promotions.forEach { promotion ->
                            if (appointment.idPromotion!!.contains(promotion.id)) {
                                Text(
                                    text = promotion.namePromotion ?: "Promoción",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF07358D)
                                )
                            }
                        }
                    } else if (!appointment.serviceId.isNullOrEmpty()) {
                        services.forEach { service ->
                            if (appointment.serviceId!!.contains(service.id)) {
                                Text(
                                    text = service.nameService ?: "Servicio",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF07358D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // DERECHA -> HORA
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFE6F0FF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp) // padding interno del chip
            ) {
                Text(
                    text = appointment.timeAppointment ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}
