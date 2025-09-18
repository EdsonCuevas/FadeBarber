package com.example.fadebarber.ui.client.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier) {

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedService by remember { mutableStateOf<ServiceData?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        // Barra superior con buscador y carrito
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F1F1))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Search", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { print("XD") }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Banner
        Image(
            painter = painterResource(id = R.drawable.perfil), // pon tu imagen aquí
            contentDescription = "Banner barbería",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(16.dp))

        // Nombre barbería y dirección
        Text("BARBER SHOP", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Avenida Miguel De La Madrid No.234, La Jaras",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // Tabs (simple)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Servicios", fontWeight = FontWeight.Bold)
            Text("Combos", color = Color.Gray)
            Text("Nosotros", color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        // Sección servicios
        Text("Servicios", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        // Tarjeta Servicio 1
        ServiceCard(
            service = ServiceData(
                title = "Corte + Barba",
                description = "Corte moderno y definido, pensado para resaltar el estilo personal.",
                duration = "45 min",
                rating = "4.5/5",
                price = "$250 MXN",
                imageRes = R.drawable.perfil
            ),
            onClick = {
                selectedService = it
                scope.launch { sheetState.show() }
            }
        )

        Spacer(Modifier.height(12.dp))

        ServiceCard(
            service = ServiceData(
                title = "Corte + Bigote",
                description = "Corte moderno y definido, pensado para resaltar el estilo personal.",
                duration = "45 min",
                rating = "4.6/5",
                price = "$230 MXN",
                imageRes = R.drawable.perfil
            ),
            onClick = {
                selectedService = it
                scope.launch { sheetState.show() }
            }
        )

    }

    // BottomSheet de agendar cita
    if (selectedService != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        selectedService = null
                    }
                }
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AgendaForm(
                service = selectedService!!,
                onConfirm = { barbero, fecha, hora ->
                    // Aquí iría la lógica de guardar la cita en Firebase, etc.
                    println("Agendado: ${selectedService!!.title} con $barbero el $fecha a las $hora")
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedService = null
                    }
                }
            )
        }
    }

}

data class ServiceData(
    val title: String,
    val description: String,
    val duration: String,
    val rating: String,
    val price: String,
    val imageRes: Int
)

@Composable
fun ServiceCard(service: ServiceData, onClick: (ServiceData) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(service) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = service.imageRes),
                contentDescription = service.title,
                modifier = Modifier.size(60.dp),
                tint = Color.Unspecified
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(service.description, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("⏱ ${service.duration}   ⭐ ${service.rating}", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(service.price, fontWeight = FontWeight.Bold)
            }

            // Botón de acción (+)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A1F66)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaForm(service: ServiceData, onConfirm: (String, LocalDate, LocalTime) -> Unit) {
    var selectedBarber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val barberos = listOf("Carlos", "Luis", "Miguel")

    // Para fecha y hora
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
        Text(service.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(service.description, fontSize = 14.sp)
        Text("⏱ ${service.duration}   ⭐ ${service.rating}")
        Text("Precio: ${service.price}", fontWeight = FontWeight.Bold)

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


        // Botón confirmar
        Button(
            onClick = {
                if (selectedBarber.isNotEmpty()) {
                    onConfirm(selectedBarber, selectedDate, selectedTime)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Agendar cita")
        }
    }
}