package com.example.fadebarber.ui.client.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.HomeTab
import com.example.fadebarber.ui.client.components.PromotionCard
import com.example.fadebarber.ui.client.components.ServiceCard


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel()) {

    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Servicios) }

    val services by viewModel.services.collectAsState() // aquí llegan de Firebase
    val promotions by viewModel.promotions.collectAsState()


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
        TabRow(
            selectedTabIndex = when (selectedTab) {
                is HomeTab.Servicios -> 0
                is HomeTab.Combos -> 1
                is HomeTab.Nosotros -> 2
            },
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF0A1F66),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(
                        tabPositions[
                            when (selectedTab) {
                                is HomeTab.Servicios -> 0
                                is HomeTab.Combos -> 1
                                is HomeTab.Nosotros -> 2
                            }
                        ]
                    ),
                    color = Color(0xFF0A1F66), // 🔹 azul oscuro
                    height = 3.dp              // puedes ajustar el grosor
                )
            }
        ) {
            listOf(HomeTab.Servicios, HomeTab.Combos, HomeTab.Nosotros).forEach { tab ->
                Tab(
                    selected = selectedTab::class == tab::class,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            is HomeTab.Servicios -> {
                Spacer(Modifier.height(12.dp))
                services.forEach { service ->
                    ServiceCard(
                        service = service,
                        onClick = {
                            selectedService = it
                            scope.launch { sheetState.show() }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            is HomeTab.Combos -> {
                promotions.forEach { promo ->
                    PromotionCard(promotion = promo, allServices = services) {
                        // ⚡ aquí decides:
                        // 1) abrir el mismo BottomSheet de agendar
                        // 2) o abrir uno distinto especial para promos
                        scope.launch { sheetState.show() }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }


            is HomeTab.Nosotros -> {
                Text("Nosotros", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Aquí información de la barbería: historia, equipo, etc.")
            }
        }

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
                    println("Agendado: ${selectedService!!} con $barbero el $fecha a las $hora")
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedService = null
                    }
                }
            )
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
        Text(service.nameService.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(service.descriptionService.toString(), fontSize = 14.sp)
        Text("⏱ ${service.durationService}   ⭐ 4.2")
        Text("Precio: ${service.priceService}", fontWeight = FontWeight.Bold)

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

        if (showTimePicker) {

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