package com.example.fadebarber.ui.client.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.collections.forEach

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaPromoForm(
    promotion: PromotionData,
    barbers: List<UserData>, // 👈 lo recibimos del repo
    userId: String,
    onConfirm: (Boolean, String) -> Unit

) {
    var selectedBarber by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

    val times = listOf("5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM")
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Agendar Cita", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // 🔹 Card de servicio
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(promotion.namePromotion.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("⏱ ${promotion.durationPromotion} min")
                Spacer(Modifier.height(4.dp))
                Text("💵 ${promotion.pricePromotion} MXN", fontWeight = FontWeight.Bold)
            }
        }

        // 🔹 Barberos reales desde Firebase
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
                        .clickable { selectedBarber = barber.id }, // guardamos el ID
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

        // 🔹 Confirmar
        Button(
            onClick = {
                if (selectedBarber != null && selectedTime != null) {
                    val appointment = AppointmentPromotion(
                        idPromotion = promotion.id,
                        idClient = userId,
                        idEmployee = selectedBarber!!,
                        dateAppointment = selectedDate.toString(),
                        timeAppointment = selectedTime.toString(),
                        statusAppointment = 1
                    )
                    scope.launch {
                        val success = FirebaseRepository.saveAppointment(appointment)
                        if (success) {
                            onConfirm(true, "Cita agendada")
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
            Text("Agendar")
        }
    }
}