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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.fadebarber.utils.notificarClienteReagendamiento
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CitaPage(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: DashboardViewModel = viewModel()
) {
    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }
    var calendarExpanded by remember { mutableStateOf(false) }
    val allowedMinMonth = remember { YearMonth.from(LocalDate.now()).minusMonths(1) }
    val allowedMaxMonth = remember { YearMonth.from(LocalDate.now()).plusMonths(1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rescheduleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val promotions by viewModel.promotions.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

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

        if (!calendarExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                ),
                onClick = { calendarExpanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentMonth.month.toString().lowercase(Locale("es")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            MonthCalendarEmployee(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                appointments = appointments,
                currentEmployeeId = user.id,
                enabledDates = days.toSet(),
                onMonthChange = { newMonth ->
                    if (newMonth >= allowedMinMonth && newMonth <= allowedMaxMonth) {
                        currentMonth = newMonth
                    }
                },
                onSelectDate = { date ->
                    selectedDate = date
                    calendarExpanded = false
                },
                allowedMinMonth = allowedMinMonth,
                allowedMaxMonth = allowedMaxMonth,
                onCollapse = { calendarExpanded = false },
                isExpanded = calendarExpanded
            )
        }

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

                    val canReschedule = appointment.statusAppointment == 1
                    var showReschedule by remember { mutableStateOf(false) }
                    if (canReschedule) {
                        Button(
                            onClick = { showReschedule = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Reagendar", color = Color.White)
                        }
                    }

                    if (showReschedule) {
                        var rescheduleDate by remember { mutableStateOf(LocalDate.parse(appointment.dateAppointment)) }
                        var rescheduleTime by remember { mutableStateOf(LocalTime.parse(appointment.timeAppointment)) }
                        var calendarExpanded by remember { mutableStateOf(false) }

                        ModalBottomSheet(
                            sheetState = rescheduleSheetState,
                            containerColor = Color.White,
                            onDismissRequest = { showReschedule = false }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Reagendar cita", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                                // Selector de fecha (colapsable)
                                if (!calendarExpanded) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        onClick = { calendarExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White)
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rescheduleDate.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E293B)
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color(0xFF1E293B),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                } else {
                                    MonthCalendarEmployee(
                                        currentMonth = YearMonth.from(rescheduleDate),
                                        selectedDate = rescheduleDate,
                                        appointments = appointments,
                                        currentEmployeeId = user.id,
                                        enabledDates = days.toSet(),
                                        onMonthChange = { newMonth -> },
                                        onSelectDate = { date ->
                                            rescheduleDate = date
                                            calendarExpanded = false
                                        },
                                        allowedMinMonth = YearMonth.from(today).minusMonths(1),
                                        allowedMaxMonth = YearMonth.from(today).plusMonths(1),
                                        onCollapse = { calendarExpanded = false },
                                        isExpanded = true
                                    )
                                }

                                // Selector de hora con slots disponibles
                                val employeeSchedule = users.firstOrNull { it.id == user.id }?.schedule
                                val dayKey = rescheduleDate.dayOfWeek.name.lowercase(Locale.ENGLISH)
                                val daySchedule = employeeSchedule?.get(dayKey)

                                val activeAppointments = appointments.filter {
                                    it.idEmployee == user.id && it.dateAppointment == rescheduleDate.toString() && it.id != appointment.id && (it.statusAppointment ?: 1) < 4
                                }
                                val occupiedSlots = activeAppointments.flatMap { appt ->
                                    val start = LocalTime.parse(appt.timeAppointment, DateTimeFormatter.ofPattern("HH:mm"))
                                    val duration = appt.durationTotal ?: 0
                                    generateSequence(start) { it.plusMinutes(30) }
                                        .takeWhile { it.isBefore(start.plusMinutes(duration.toLong())) }
                                        .map { it.format(DateTimeFormatter.ofPattern("HH:mm")) }
                                }.toSet()

                                if (daySchedule != null && daySchedule.available && daySchedule.start != null && daySchedule.end != null) {
                                    val start = LocalTime.parse(daySchedule.start, DateTimeFormatter.ofPattern("HH:mm"))
                                    val end = LocalTime.parse(daySchedule.end, DateTimeFormatter.ofPattern("HH:mm"))

                                    Text("Selecciona nueva hora", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        generateSequence(start) { it.plusMinutes(30) }
                                            .takeWhile { !it.isAfter(end) }
                                            .forEach { slot ->
                                                val slotKey = slot.format(DateTimeFormatter.ofPattern("HH:mm"))
                                                val isOccupied = occupiedSlots.contains(slotKey)
                                                val isPastTime = rescheduleDate == LocalDate.now() && slot.isBefore(LocalTime.now())
                                                val isSelected = rescheduleTime.format(DateTimeFormatter.ofPattern("HH:mm")) == slotKey

                                                Button(
                                                    onClick = { if (!isOccupied && !isPastTime) rescheduleTime = slot },
                                                    enabled = !isOccupied && !isPastTime,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = when {
                                                            isSelected -> Color(0xFF2563EB)
                                                            isOccupied || isPastTime -> Color(0xFFF1F5F9)
                                                            else -> Color(0xFFF8FAFC)
                                                        },
                                                        contentColor = when {
                                                            isSelected -> Color.White
                                                            isOccupied || isPastTime -> Color(0xFF94A3B8)
                                                            else -> Color(0xFF1E293B)
                                                        }
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(slot.format(DateTimeFormatter.ofPattern("HH:mm")))
                                                }
                                            }
                                    }
                                }

                                // Confirmar reagendar
                                val dbFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                val totalDuration = appointment.durationTotal ?: 0
                                val requestedSlots = generateSequence(rescheduleTime) { it.plusMinutes(30) }
                                    .takeWhile { it.isBefore(rescheduleTime.plusMinutes(totalDuration.toLong())) }
                                    .map { it.format(dbFormatter) }
                                    .toSet()

                                val conflict = requestedSlots.any { it in occupiedSlots }

                                Button(
                                    onClick = {
                                        if (conflict) return@Button

                                        // Mostrar loading o deshabilitar botón si quieres
                                        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                                            .getReference("Appointment")

                                        val updates: Map<String, Any> = mapOf(
                                            "dateAppointment" to rescheduleDate.toString(),
                                            "timeAppointment" to rescheduleTime.format(dbFormatter)
                                        )

                                        appointment.id?.let { id ->
                                            database.child(id)
                                                .updateChildren(updates)
                                                .addOnSuccessListener {
                                                    println("✅ Cita reagendada en Firebase")

                                                    // Enviar notificación al cliente
                                                    scope.launch {
                                                        try {
                                                            val notificacionEnviada = withContext(Dispatchers.IO) {
                                                                notificarClienteReagendamiento(
                                                                    appointmentId = id,
                                                                    nuevaFecha = rescheduleDate.toString(),
                                                                    nuevaHora = rescheduleTime.format(dbFormatter),
                                                                    employeeName = user.nameUser,
                                                                )
                                                            }

                                                            successMessage = if (notificacionEnviada) {
                                                                "Cita reagendada y cliente notificado ✅"
                                                            } else {
                                                                "Cita reagendada (notificación pendiente) ⚠️"
                                                            }

                                                            showSuccessDialog = true


                                                        } catch (e: Exception) {
                                                            println("❌ Error al notificar: ${e.message}")
                                                        }
                                                    }
                                                    // Cerrar el modal
                                                    showReschedule = false
                                                    selectedAppointment = null

                                                }
                                                .addOnFailureListener { error ->
                                                    println("❌ Error al reagendar cita: ${error.message}")
                                                    // Aquí podrías mostrar un Toast o Snackbar con el error
                                                }
                                        }
                                    },
                                    enabled = !conflict,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!conflict) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                                    )
                                ) {
                                    Text(
                                        "Confirmar nueva fecha y hora",
                                        color = if (!conflict) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Éxito") },
            text = { Text(successMessage) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                    Text("Cerrar")
                }
            }
        )
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

@Composable
private fun MonthCalendarEmployee(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointments: List<AppointmentClientData>,
    currentEmployeeId: String?,
    enabledDates: Set<LocalDate>,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    allowedMinMonth: YearMonth,
    allowedMaxMonth: YearMonth,
    onCollapse: () -> Unit,
    isExpanded: Boolean
) {
    val locale = Locale("es", "ES")
    val monthTitle = currentMonth.month
        .getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString() } + " " + currentMonth.year

    val appointmentDates = remember(appointments, currentEmployeeId) {
        appointments
            .filter { it.idEmployee == currentEmployeeId }
            .mapNotNull { it.dateAppointment?.take(10) }
            .toSet()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                        modifier = Modifier.size(36.dp),
                        enabled = currentMonth > allowedMinMonth
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = null,
                            tint = if (currentMonth > allowedMinMonth) Color(0xFF2563EB) else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                        modifier = Modifier.size(36.dp),
                        enabled = currentMonth < allowedMaxMonth
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_next),
                            contentDescription = null,
                            tint = if (currentMonth < allowedMaxMonth) Color(0xFF2563EB) else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CalendarGridEmployee(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                appointmentDates = appointmentDates,
                enabledDates = enabledDates,
                onDateClick = onSelectDate
            )
        }
    }
}

@Composable
private fun CalendarGridEmployee(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    appointmentDates: Set<String>,
    enabledDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    val cells = mutableListOf<LocalDate?>().apply {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) {
            add(currentMonth.atDay(day))
        }
        while (size % 7 != 0) add(null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (rowStart in cells.indices step 7) {
            if (rowStart >= cells.size) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 7) {
                    val date = cells.getOrNull(rowStart + i)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CalendarDayCellEmployee(
                            date = date,
                            isSelected = date == selectedDate,
                            hasAppointments = date?.toString() in appointmentDates,
                            enabled = date != null && enabledDates.contains(date),
                            onClick = { date?.let { onDateClick(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCellEmployee(
    date: LocalDate?,
    isSelected: Boolean,
    hasAppointments: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    if (date == null) {
        Box(modifier = Modifier.size(44.dp))
        return
    }

    val today = LocalDate.now()
    val isToday = date == today

    val bg = when {
        isSelected -> Color(0xFF2563EB)
        isToday -> Color(0xFFE3F2FD)
        else -> Color.Transparent
    }

    val fg = when {
        !enabled -> Color(0xFF94A3B8)
        isSelected -> Color.White
        isToday -> Color(0xFF2563EB)
        else -> Color(0xFF1E293B)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 15.sp,
                color = fg,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (enabled && hasAppointments && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                )
            }
        }
    }
}
