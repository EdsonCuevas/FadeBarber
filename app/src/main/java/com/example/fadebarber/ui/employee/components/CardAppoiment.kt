package com.example.fadebarber.ui.employee.components

import android.R.attr.fontWeight
import android.icu.text.DateFormat
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.data.model.AppointmentData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import formatDate

@Composable
fun CardAppointment(
    appointment: AppointmentData,
    services: List<ServiceData>,
    users: List<UserData>,
    promotions: List<PromotionData>,
    onAppointmentClick: (AppointmentData) -> Unit
) {


    Column(modifier = Modifier) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAppointmentClick(appointment) }
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (appointment.idPromotion != 0) {

                        val promotion = promotions.find { it.id == appointment.idPromotion }
                        promotion?.let {
                            Text(
                                text = it.namePromotion.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    } else {
                        val service = services.find { it.id == appointment.serviceId }
                        service?.let {
                            Text(
                                text = it.nameService.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                        contentDescription = "Check",
                        modifier = Modifier.size(25.dp),
                        tint = Color.Black
                    )

                }

                Spacer(modifier = Modifier.height(2.dp))
                if(appointment?.idClient != ""){
                    val cliente = users.find { it.id == appointment.idClient }
                    Log.d("cliente", cliente.toString())
                    cliente?.let {
                        Text(
                            text = "Cliente: ${it.nameUser}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else{
                    Text(
                        text = "Cliente: ${appointment.nameUser}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(appointment.dateAppointment),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

            }
        }

        // Spacer ahora dentro del Column principal
        Spacer(modifier = Modifier.height(8.dp))
    }

}