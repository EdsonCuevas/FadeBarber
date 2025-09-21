package com.example.fadebarber.ui.client.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.data.model.PromotionData
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaPromoForm(promotion: PromotionData, onConfirm: (String, LocalDate, LocalTime) -> Unit) {
    var selectedBarber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val barberos = listOf("Carlos", "Luis", "Miguel")

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(promotion.namePromotion ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("⏱ ${promotion.durationPromotion} min   ⭐ 4.5")
        Text("Precio especial: $${promotion.pricePromotion}", fontWeight = FontWeight.Bold)

        // Selección de barbero
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedBarber,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecciona un barbero") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                barberos.forEach { barber ->
                    DropdownMenuItem(
                        text = { Text(barber) },
                        onClick = {
                            selectedBarber = barber
                            expanded = false
                        }
                    )
                }
            }
        }

        // Selección de fecha
        Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Fecha: $selectedDate")
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = System.currentTimeMillis()
                    ),
                    showModeToggle = false
                )
            }
        }

        // Selección de hora
        Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Hora: $selectedTime")
        }

        // Confirmar
        Button(
            onClick = {
                if (selectedBarber.isNotEmpty()) {
                    onConfirm(selectedBarber, selectedDate, selectedTime)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Agendar promoción")
        }
    }
}
