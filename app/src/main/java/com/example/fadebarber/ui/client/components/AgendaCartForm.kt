package com.example.fadebarber.ui.client.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.AppointmentPromotion
import com.example.fadebarber.data.model.AppointmentService
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AgendaCartForm(
    items: List<Any>, // Puede contener ServiceData o PromotionData
    barbers: List<UserData>,
    userId: String,
    onConfirm: (Boolean, String) -> Unit
) {
    var selectedBarber by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    var selectedPayment by remember { mutableStateOf("Efectivo") }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

    val times = listOf("5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM")
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    val scope = rememberCoroutineScope()

    val total = items.sumOf {
        when (it) {
            is ServiceData -> it.priceService ?: 0
            is PromotionData -> it.pricePromotion ?: 0
            else -> 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Agendar Carrito", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // 🔹 Selección de barbero
        Text("Selecciona un barbero", fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            barbers.forEach { barber ->
                val isSelected = selectedBarber == barber.id
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedBarber = barber.id },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0A66C2) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color.Gray
                        )
                        Text(
                            barber.nameUser,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // 🔹 Fechas
        Text("Selecciona la fecha", fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days) { day ->
                val isSelected = selectedDate == day
                Card(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { selectedDate = day },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0A66C2) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                        Text(
                            text = day.month.name.take(3),
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }
        }

        // 🔹 Horarios
        Text("Selecciona la hora", fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            times.forEach { time ->
                val parsedTime = LocalTime.parse(time, formatter)
                val isSelected = selectedTime == parsedTime
                Button(
                    onClick = { selectedTime = parsedTime },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF0A66C2) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Text(time, color = if (isSelected) Color.White else Color.Black)
                }
            }
        }

        // 🔹 Método de pago + Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SelectBox (Dropdown estilizado)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0A66C2))
                ) {
                    if (selectedPayment == "Efectivo") Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF0A66C2))
                    else if (selectedPayment == "Tarjeta") Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF0A66C2))
                    Spacer(Modifier.width(8.dp))
                    Text(selectedPayment)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp))
                        .width(150.dp)
                ) {
                    listOf("Efectivo", "Tarjeta").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (option == "Tarjeta") Icons.Default.CreditCard else Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = if (selectedPayment == option) Color(0xFF0A66C2) else Color.Gray
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        option,
                                        fontWeight = if (selectedPayment == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedPayment == option) Color(0xFF0A66C2) else Color.Black
                                    )
                                }
                            },
                            onClick = {
                                selectedPayment = option
                                expanded = false
                            },
                            modifier = Modifier
                                .background(
                                    if (selectedPayment == option) Color(0xFFE3F2FD) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Total
            Text(
                text = "Total: $${total} MXN",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF0A66C2)
            )
        }

        // 🔹 Confirmar agendado de TODO el carrito
        Button(
            onClick = {
                if (selectedBarber != null && selectedTime != null) {
                    scope.launch {
                        // separar ids
                        val serviceIds = items.filterIsInstance<ServiceData>().map { it.id }
                        val promoIds = items.filterIsInstance<PromotionData>().map { it.id }

                        // separar precios
                        val servicePrice = items.filterIsInstance<ServiceData>().sumOf { it.priceService }
                        val promotionPrice = items.filterIsInstance<PromotionData>().sumOf { it.pricePromotion }

                        val appointment = AppointmentClientData(
                            idClient = userId,
                            idEmployee = selectedBarber!!,
                            serviceId = serviceIds,
                            idPromotion = promoIds,
                            dateAppointment = selectedDate.toString(),
                            timeAppointment = selectedTime.toString(),
                            methodPayment = selectedPayment,
                            totalPrice = servicePrice + promotionPrice,
                            statusAppointment = 1
                        )

                        val success = FirebaseRepository.saveAppointment(appointment)
                        if (success) {
                            onConfirm(true, "Cita agendada con ${serviceIds.size} servicios y ${promoIds.size} promos 🎉")
                        } else {
                            onConfirm(false, "Error al guardar ❌ Intenta de nuevo")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2))
        ) {
            Text("Agendar Carrito")
        }
    }
}
