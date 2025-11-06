package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.PromotionData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaPage(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: DashboardViewModel = viewModel()
) {
    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val promotions by viewModel.promotions.collectAsState(initial = emptyList())

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

    // Filtrar citas por fecha seleccionada y empleado
    val filteredAppointments = appointments
        .filter { appointment ->
            appointment.idEmployee == user.id &&
                    LocalDate.parse(appointment.dateAppointment) == selectedDate
        }
        .sortedBy {
            LocalTime.parse(it.timeAppointment, DateTimeFormatter.ofPattern("HH:mm"))
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // HEADER CON TÍTULO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF3B82F6)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reloj),
                            contentDescription = "Calendario",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Citas de Barbería",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es")))
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es")) else it.toString() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selecciona una fecha",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SELECTOR DE FECHAS
        // SELECTOR DE FECHAS
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(days) { day ->
                val isSelected = selectedDate == day
                val isToday = day == today

                // Verificar si hay citas en este día
                val hasCitas = appointments.any { appointment ->
                    appointment.idEmployee == user.id &&
                            LocalDate.parse(appointment.dateAppointment) == day
                }

                Card(
                    modifier = Modifier
                        .width(60.dp)
                        .clickable { selectedDate = day },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> Color(0xFF1E3A8A)
                            isToday -> Color(0xFFEBF8FF)
                            else -> Color.White
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 6.dp else 2.dp
                    )
                )  {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.format(DateTimeFormatter.ofPattern("EEE", Locale("es"))).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isSelected -> Color.White
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF6B7280)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF1F2937)
                            }
                        )
                        Text(
                            text = day.format(DateTimeFormatter.ofPattern("MMM", Locale("es"))).uppercase(),
                            fontSize = 10.sp,
                            color = when {
                                isSelected -> Color.White.copy(alpha = 0.8f)
                                isToday -> Color(0xFF3B82F6)
                                else -> Color(0xFF9CA3AF)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HEADER CITAS DEL DÍA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedDate == today) "Citas De Hoy" else "Citas del Día",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = "${filteredAppointments.size} citas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LISTA DE CITAS
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredAppointments.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_clock),
                            contentDescription = "Sin citas",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay citas para este día",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else {
                items(filteredAppointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        services = services,
                        users = users,
                        promotions = promotions,
                        onClick = { selectedAppointment = it }
                    )
                }
            }
        }
    }
    if (selectedAppointment != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = Color.White,
            onDismissRequest = { selectedAppointment = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                selectedAppointment?.let { appointment ->

                    val statusInfo = when (appointment.statusAppointment) {
                        1 -> Pair("Pendiente", Color(0xFFF59E0B))
                        2 -> Pair("En curso", Color(0xFF22C55E))
                        3 -> Pair("Completada", Color(0xFF3B82F6))
                        4 -> Pair("Cancelada", Color(0xFFEF4444))
                        else -> Pair("Desconocido", Color.Gray)
                    }

                    // Header - Estado de la Cita
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estado de la Cita",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(
                                    statusInfo.second.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusInfo.second, CircleShape)
                            )
                            Text(
                                text = statusInfo.first,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusInfo.second
                            )
                        }


                    }

                    // Card Principal del Servicio
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            // Icono y Título del Servicio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Icono del servicio
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Color(0xFFE6F0FF),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✂",
                                        fontSize = 24.sp
                                    )
                                }

                                Column {
                                    // Título del servicio
                                    val titles = mutableListOf<String>()

                                    // Promociones
                                    if (appointment.idPromotion.isNotEmpty()) {
                                        val validPromotionIds = appointment.idPromotion.filterNotNull()
                                        if (validPromotionIds.isNotEmpty()) {
                                            val firstPromotion = promotions.find {
                                                it.id == validPromotionIds.firstOrNull()
                                            }
                                            if (firstPromotion != null) {
                                                val totalPromotions = validPromotionIds.size
                                                titles.add(
                                                    if (totalPromotions > 1) {
                                                        "${firstPromotion.namePromotion} + ${totalPromotions - 1} más"
                                                    } else {
                                                        firstPromotion.namePromotion ?: "Promoción"
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Servicios
                                    if (appointment.serviceId.isNotEmpty()) {
                                        val validServiceIds = appointment.serviceId.filterNotNull()
                                        val selectedServices = services.filter { it.id in validServiceIds }
                                        if (selectedServices.isNotEmpty()) {
                                            titles.add(
                                                if (selectedServices.size > 1) {
                                                    "${selectedServices[0].nameService} + ${selectedServices.size - 1} más"
                                                } else {
                                                    selectedServices[0].nameService ?: "Servicio"
                                                }
                                            )
                                        }
                                    }

                                    if (titles.isNotEmpty()) {
                                        Text(
                                            text = titles[0],
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    // Descripción con el profesional
                                    val user = users.find { it.id == appointment.idClient }
                                    Text(
                                        text = "con ${user?.nameUser ?: appointment.nameClient ?: "Sin cliente"}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Información de la cita
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Fecha
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = "Fecha",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = appointment?.dateAppointment.orEmpty(),
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                // Hora
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = "Hora",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = appointment.timeAppointment ?: "0:00",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Precio
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AttachMoney,
                                        contentDescription = "Precio",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "${appointment.totalPrice ?: "0"}.00 ${appointment.methodPayment ?: ""}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                // Duración
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.HourglassBottom,
                                        contentDescription = "Duración",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "${appointment.durationTotal ?: "0"} min",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    // Lista de Servicios a Realizar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Detalles del servicio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )

                            // Mostrar servicios de promociones
                            if (appointment.idPromotion.isNotEmpty()) {
                                appointment.idPromotion.filterNotNull().forEach { promotionId ->
                                    val promotion = promotions.find { it.id == promotionId }
                                    promotion?.let { promo ->
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Promoción: ${promo.namePromotion}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF7C3AED),
                                            )

                                            promo.servicePromotion?.forEach { serviceId ->
                                                val service = services.find { it.id == serviceId }
                                                service?.let { srv ->
                                                    Row(
                                                        modifier = Modifier.padding(start = 12.dp),
                                                        verticalAlignment = Alignment.Top,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(
                                                                    Color(0xFF64748B),
                                                                    CircleShape
                                                                )
                                                                .padding(top = 8.dp)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = srv.nameService ?: "Servicio",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFF374151)
                                                            )
                                                            if (!srv.descriptionService.isNullOrBlank()) {
                                                                Text(
                                                                    text = srv.descriptionService,
                                                                    fontSize = 12.sp,
                                                                    color = Color(0xFF6B7280)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Mostrar servicios individuales
                            if (appointment.serviceId.isNotEmpty()) {
                                appointment.serviceId.filterNotNull().forEach { serviceId ->
                                    val service = services.find { it.id == serviceId }
                                    service?.let { srv ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        Color(0xFF3B82F6),
                                                        CircleShape
                                                    )
                                                    .padding(top = 8.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = srv.nameService ?: "Servicio",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF374151)
                                                )
                                                if (!srv.descriptionService.isNullOrBlank()) {
                                                    Text(
                                                        text = srv.descriptionService,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF6B7280)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Si no hay servicios
                            if (appointment.serviceId.isEmpty() && appointment.idPromotion.isEmpty()) {
                                Text(
                                    text = "No hay servicios especificados",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }


                    // Espaciado adicional para el bottom sheet
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentClientData,
    services: List<ServiceData>,
    users: List<UserData>,
    promotions: List<PromotionData>,
    onClick: (AppointmentClientData) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determinar color según estado
    val statusColor = when (appointment.statusAppointment) {
        1 -> Color(0xFFF59E0B)
        2 -> Color(0xFF22C55E)
        3 -> Color(0xFF3B82F6)
        4 -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val statusText = when (appointment.statusAppointment) {
        1 -> "Pendiente"
        2 -> "En curso"
        3 -> "Completada"
        4 -> "Cancelada"
        else -> "Desconocido"
    }

    // Obtener nombre del cliente
    val client = users.find { it.id == appointment.idClient }

    // Obtener servicios
    val titles = mutableListOf<String>()

    // Promociones
    if (appointment.idPromotion.isNotEmpty()) {
        val validPromotionIds = appointment.idPromotion.filterNotNull()
        if (validPromotionIds.isNotEmpty()) {
            val firstPromotion = promotions.find { it.id == validPromotionIds.firstOrNull() }
            if (firstPromotion != null) {
                val totalPromotions = validPromotionIds.size
                titles.add(
                    if (totalPromotions > 1) {
                        "${firstPromotion.namePromotion} + ${totalPromotions - 1} más"
                    } else {
                        firstPromotion.namePromotion ?: "Promoción"
                    }
                )
            }
        }
    }

    // Servicios
    if (appointment.serviceId.isNotEmpty()) {
        val validServiceIds = appointment.serviceId.filterNotNull()
        val selectedServices = services.filter { it.id in validServiceIds }
        if (selectedServices.isNotEmpty()) {
            val firstService = selectedServices[0]
            titles.add(
                if (selectedServices.size > 1) {
                    "${firstService.nameService} + ${selectedServices.size - 1} más"
                } else {
                    firstService.nameService ?: "Servicio"
                }
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onClick(appointment) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color según estado
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(statusColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Hora
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Hora",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appointment.timeAppointment ?: "00:00",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nombre del cliente
                Text(
                    text = client?.nameUser ?: appointment.nameClient ?: "Sin cliente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Servicio
                Text(
                    text = titles.firstOrNull() ?: "Sin servicio",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Badge de estado
            Box(
                modifier = Modifier
                    .background(
                        statusColor.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
        }
    }
}