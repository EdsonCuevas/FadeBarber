    package com.example.fadebarber.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IZQUIERDA -> ICONO + INFO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // 🔹 ocupa todo el espacio disponible a la izquierda
            ) {
                // Círculo con ícono
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFF3B82F6),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scissor), // Necesitarás este icono
                        contentDescription = "Calendario",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                }

                Spacer(modifier = Modifier.width(12.dp))

                // Servicio + Cliente
                Column {
                    // 🔹 Texto final del título
                    val titles = mutableListOf<String>()

                    // 🔹 Promociones (contando ocurrencias)
                    if (appointment.idPromotion.isNotEmpty()) {
                        val validPromotionIds = appointment.idPromotion.filterNotNull()

                        if (validPromotionIds.isNotEmpty()) {
                            val firstPromotion = promotions.find { it.id == validPromotionIds.firstOrNull()}

                            if (firstPromotion != null) {
                                val totalPromotions = validPromotionIds.size
                                if (totalPromotions > 1) {
                                    titles.add("${firstPromotion.namePromotion} + ${totalPromotions - 1} más")
                                } else {
                                    titles.add(firstPromotion.namePromotion ?: "Promoción")
                                }
                            }
                        }
                    }

                    // 🔹 Servicios (únicos)
                    if (appointment.serviceId.isNotEmpty()) {
                        val validServiceIds = appointment.serviceId.filterNotNull()
                        val selectedServices = services.filter { it.id in validServiceIds }

                        if (selectedServices.isNotEmpty()) {
                            if (selectedServices.size > 1) {
                                titles.add("${selectedServices[0].nameService} + ${selectedServices.size - 1} más")
                            } else {
                                titles.add(selectedServices[0].nameService ?: "Servicio")
                            }
                        }
                    }

                    // 🔹 Texto final
                    if (titles.isNotEmpty()) {
                        val displayText = if (titles.size > 1) {
                            "${titles[0]} + ${titles.size - 1} más"
                        } else {
                            titles[0]
                        }

                        Text(
                            text = displayText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF07358D)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // DERECHA -> HORA (fija a la derecha)
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFE6F0FF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = appointment.timeAppointment ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }

            Row {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown, // Aquí pones el icono que quieras
                    contentDescription = "Cliente",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

        }

    }
}
