package com.example.fadebarber.ui.client.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.repository.FirebaseRepository
import com.example.fadebarber.services.generateQRCode
import formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sendEmailWithQR
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Estados de procesamiento
enum class PaymentState {
    IDLE,           // Esperando acción del usuario
    PROCESSING,     // Procesando pago
    SUCCESS,        // Pago exitoso
    ERROR           // Error en el pago
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgendaCartForm(
    items: List<Any>,
    barbers: List<UserData>,
    userId: String,
    onConfirm: (Boolean, String) -> Unit,
    user: UserData
) {
    var selectedBarber by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedPayment by remember { mutableStateOf("Efectivo") }

    // Estados de procesamiento
    var paymentState by remember { mutableStateOf(PaymentState.IDLE) }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val dbFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val displayFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    val scope = rememberCoroutineScope()

    var occupiedTimeStrings by remember { mutableStateOf<Set<String>>(emptySet()) }
    var currentAppts by remember { mutableStateOf<List<AppointmentClientData>>(emptyList()) }

    // 🎯 Hook para manejar el pago con Stripe con control de múltiples intentos
    val iniciarPagoStripe = rememberStripePayment(
        onPaymentSuccess = {
            scope.launch {
                paymentState = PaymentState.PROCESSING

                try {
                    // Construir la cita después del pago exitoso
                    val requestedStart = selectedTime!!
                    val totalDuration = items.sumOf {
                        when (it) {
                            is ServiceData -> it.durationService ?: 0
                            is PromotionData -> it.durationPromotion ?: 0
                            else -> 0
                        }
                    }
                    val requestedEnd = requestedStart.plusMinutes(totalDuration.toLong())
                    val requestedSlots = generateSequence(requestedStart) { it.plusMinutes(30) }
                        .takeWhile { it.isBefore(requestedEnd) }
                        .map { it.format(dbFormatter) }
                        .toSet()

                    val serviceIds = items.filterIsInstance<ServiceData>().mapNotNull { it.id }
                    val promoIds = items.filterIsInstance<PromotionData>().mapNotNull { it.id }
                    val servicePrice = items.filterIsInstance<ServiceData>().sumOf { it.priceService ?: 0 }
                    val promotionPrice = items.filterIsInstance<PromotionData>().sumOf { it.pricePromotion ?: 0 }

                    val appointment = AppointmentClientData(
                        id = null,
                        idClient = userId,
                        idEmployee = selectedBarber!!,
                        serviceId = serviceIds,
                        idPromotion = promoIds,
                        dateAppointment = selectedDate.toString(),
                        timeAppointment = requestedStart.format(dbFormatter),
                        methodPayment = selectedPayment,
                        totalPrice = servicePrice + promotionPrice,
                        statusAppointment = 1,
                        statusPayment = 2,
                        durationTotal = totalDuration
                    )

                    // Guardar en Firebase
                    val appointmentId = withContext(Dispatchers.IO) {
                        FirebaseRepository.saveAppointment(appointment)
                    }

                    if (appointmentId != null) {
                        // Obtener nombre del barbero
                        val nameBarber = barbers.firstOrNull { it.id == selectedBarber }?.nameUser ?: ""

                        // Generar QR con el ID de la cita
                        val qrBitmap = withContext(Dispatchers.IO) {
                            generateQRCode(appointmentId)
                        }

                        // Enviar correo con QR
                        withContext(Dispatchers.IO) {
                            sendEmailWithQR(
                                to = user.correoUser,
                                subject = "Confirmación de cita - FadeBarber",
                                clientName = user.nameUser,
                                barberName = nameBarber,
                                date = formatDate(selectedDate.toString()),
                                time = requestedStart.format(dbFormatter),
                                qrBitmap = qrBitmap,
                                fromEmail = "elizaldiromero14@gmail.com",
                                fromPassword = "rvemlhzgtkzcqjnv"
                            )
                        }

                        occupiedTimeStrings = occupiedTimeStrings + requestedSlots
                        paymentState = PaymentState.SUCCESS
                    } else {
                        errorMessage = "Error al guardar la cita. Intenta de nuevo."
                        paymentState = PaymentState.ERROR
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                    paymentState = PaymentState.ERROR
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        },
        onPaymentFailed = { mensaje ->
            errorMessage = mensaje
            paymentState = PaymentState.ERROR
            isProcessing = false
        }
    )

    // Días disponibles
    val availableDays by remember(selectedBarber, barbers) {
        derivedStateOf {
            val schedule = barbers.firstOrNull { it.id == selectedBarber }?.schedule
            (0..6).mapNotNull { offset ->
                val day = today.plusDays(offset.toLong())
                val key = day.dayOfWeek.name.lowercase(Locale.ENGLISH)
                if (schedule?.get(key)?.available == true) day else null
            }
        }
    }

    // Cargar citas ocupadas
    LaunchedEffect(selectedBarber, selectedDate) {
        if (selectedBarber != null && selectedDate != null) {
            try {
                currentAppts = FirebaseRepository.getAppointmentsByBarberAndDate(
                    barberId = selectedBarber!!,
                    date = selectedDate.toString()
                )
                occupiedTimeStrings = currentAppts.flatMap { appt ->
                    val start = LocalTime.parse(appt.timeAppointment, dbFormatter)
                    val duration = appt.durationTotal ?: 0
                    generateSequence(start) { it.plusMinutes(30) }
                        .takeWhile { it.isBefore(start.plusMinutes(duration.toLong())) }
                        .map { it.format(dbFormatter) }
                }.toSet()
            } catch (e: Exception) {
                occupiedTimeStrings = emptySet()
                currentAppts = emptyList()
                e.printStackTrace()
            }
            selectedTime = null
        } else {
            occupiedTimeStrings = emptySet()
            selectedTime = null
        }
    }

    val total = items.sumOf {
        when (it) {
            is ServiceData -> it.priceService ?: 0
            is PromotionData -> it.pricePromotion ?: 0
            else -> 0
        }
    }

    // Diálogo de procesamiento/resultado
    if (paymentState != PaymentState.IDLE) {
        Dialog(
            onDismissRequest = {
                if (paymentState != PaymentState.PROCESSING) {
                    if (paymentState == PaymentState.SUCCESS) {
                        onConfirm(true, "Cita agendada correctamente 🎉")
                    }
                    paymentState = PaymentState.IDLE
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = paymentState != PaymentState.PROCESSING,
                dismissOnClickOutside = paymentState != PaymentState.PROCESSING
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (paymentState) {
                        PaymentState.PROCESSING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = Color(0xFF2563EB),
                                strokeWidth = 6.dp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Procesando tu cita...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Por favor espera un momento",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }

                        PaymentState.SUCCESS -> {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Color(0xFF10B981).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Éxito",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "¡Cita agendada!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Tu cita ha sido confirmada exitosamente.",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Revisa tu correo electrónico para ver los detalles y tu código QR.",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    paymentState = PaymentState.IDLE
                                    onConfirm(true, "Cita agendada correctamente 🎉")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Entendido",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        PaymentState.ERROR -> {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Color(0xFFEF4444).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Error al procesar",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                errorMessage,
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    paymentState = PaymentState.IDLE
                                    onConfirm(false, errorMessage)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Cerrar",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA)),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // HEADER
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF2563EB).copy(alpha = 0.1f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Agendar",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Agendar Cita",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // RESUMEN DE SELECCIÓN (agrupado por cantidad)
        item {
            val serviceGroups = items.filterIsInstance<ServiceData>().groupBy { it.id }
            val promotionGroups = items.filterIsInstance<PromotionData>().groupBy { it.id }
            if (serviceGroups.isNotEmpty() || promotionGroups.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "Servicios y Promos seleccionados",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Servicios agrupados
                        serviceGroups.values.forEach { group ->
                            val s = group.first()
                            val qty = group.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.nameService ?: "Servicio", fontWeight = FontWeight.SemiBold)
                                    Text("$${s.priceService} MXN", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                                Text("x$qty", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text("$${s.priceService * qty}", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Promos agrupadas
                        promotionGroups.values.forEach { group ->
                            val p = group.first()
                            val qty = group.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.namePromotion ?: "Promoción", fontWeight = FontWeight.SemiBold)
                                    Text("$${p.pricePromotion} MXN", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                                Text("x$qty", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text("$${p.pricePromotion * qty}", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // SELECCIÓN DE BARBERO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF2563EB).copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Barbero",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Selecciona tu barbero",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    containerColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFF8FAFC)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 6.dp else 0.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.2f)
                                                else Color(0xFF2563EB).copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = barber.nameUser.firstOrNull()?.uppercase() ?: "B",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF2563EB)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        barber.nameUser,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                                        textAlign = TextAlign.Center
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Seleccionado",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SELECCIÓN DE FECHA
        if (selectedBarber != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color(0xFF2563EB).copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Fecha",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Selecciona la fecha",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(availableDays) { day ->
                                val isSelected = selectedDate == day
                                val isToday = day == today

                                Card(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .clickable {
                                            selectedDate = day
                                            selectedTime = null
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isSelected -> Color(0xFF2563EB)
                                            else -> Color(0xFFF8FAFC)
                                        }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (isSelected) 6.dp else 0.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(vertical = 14.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = day.dayOfWeek.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale("es")
                                            ).uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when {
                                                isSelected -> Color.White.copy(alpha = 0.9f)
                                                else -> Color(0xFF94A3B8)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = day.dayOfMonth.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isSelected -> Color.White
                                                else -> Color(0xFF1E293B)
                                            }
                                        )
                                        if (isToday && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .size(6.dp)
                                                    .background(Color(0xFF2563EB), CircleShape)
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

        // SELECCIÓN DE HORARIO
        if (selectedDate != null && selectedBarber != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color(0xFF2563EB).copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Hora",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Selecciona la hora",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val schedule = barbers.firstOrNull { it.id == selectedBarber }?.schedule
                        val key = selectedDate!!.dayOfWeek.name.lowercase(Locale.ENGLISH)
                        val daySchedule = schedule?.get(key)

                        if (daySchedule != null && daySchedule.available && daySchedule.start != null && daySchedule.end != null) {
                            val start = LocalTime.parse(daySchedule.start, dbFormatter)
                            val end = LocalTime.parse(daySchedule.end, dbFormatter)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                generateSequence(start) { it.plusMinutes(30) }
                                    .takeWhile { !it.isAfter(end) }
                                    .forEach { slot ->
                                        val slotKey = slot.format(dbFormatter)
                                        val isOccupied = occupiedTimeStrings.contains(slotKey)
                                        val isSelected = selectedTime != null &&
                                                selectedTime!!.format(dbFormatter) == slotKey

                                        Button(
                                            onClick = { if (!isOccupied) selectedTime = slot },
                                            enabled = !isOccupied,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = when {
                                                    isSelected -> Color(0xFF2563EB)
                                                    isOccupied -> Color(0xFFF1F5F9)
                                                    else -> Color(0xFFF8FAFC)
                                                },
                                                contentColor = when {
                                                    isSelected -> Color.White
                                                    isOccupied -> Color(0xFF94A3B8)
                                                    else -> Color(0xFF1E293B)
                                                },
                                                disabledContainerColor = Color(0xFFF1F5F9),
                                                disabledContentColor = Color(0xFF94A3B8)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = if (isSelected) 4.dp else 0.dp
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                        ) {
                                            Text(
                                                slot.format(displayFormatter),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay horarios disponibles 🚫",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // RESUMEN Y MÉTODO DE PAGO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Resumen de Pago",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Método de pago
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2563EB)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (selectedPayment == "Efectivo")
                                            Icons.Default.AttachMoney else Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        selectedPayment,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            listOf("Efectivo", "Tarjeta").forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = if (option == "Tarjeta")
                                                    Icons.Default.CreditCard else Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = if (selectedPayment == option)
                                                    Color(0xFF2563EB) else Color(0xFF94A3B8)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                option,
                                                fontWeight = if (selectedPayment == option)
                                                    FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedPayment == option)
                                                    Color(0xFF1E293B) else Color(0xFF64748B)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPayment = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total a pagar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            "${total} MXN",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }

        // BOTÓN CONFIRMAR
        item {
            Button(
                onClick = {
                    // Validaciones iniciales
                    if (selectedBarber == null || selectedDate == null || selectedTime == null) {
                        onConfirm(false, "Selecciona barbero, fecha y hora")
                        return@Button
                    }

                    // Prevenir múltiples clics
                    if (isProcessing) {
                        return@Button
                    }

                    isProcessing = true

                    scope.launch {
                        try {
                            val requestedStart = selectedTime!!

                            // Calcular la duración total
                            val totalDuration = items.sumOf {
                                when (it) {
                                    is ServiceData -> it.durationService ?: 0
                                    is PromotionData -> it.durationPromotion ?: 0
                                    else -> 0
                                }
                            }

                            val requestedEnd = requestedStart.plusMinutes(totalDuration.toLong())

                            // Generar los bloques que ocuparía la cita
                            val requestedSlots = generateSequence(requestedStart) { it.plusMinutes(30) }
                                .takeWhile { it.isBefore(requestedEnd) }
                                .map { it.format(dbFormatter) }
                                .toSet()

                            // Verificar conflictos
                            val conflict = occupiedTimeStrings.any { it in requestedSlots }

                            if (conflict) {
                                isProcessing = false
                                onConfirm(false, "No hay tiempo suficiente para esta cita ⏰")
                                return@launch
                            }

                            // Procesar según método de pago
                            if (selectedPayment == "Efectivo") {
                                // Mostrar pantalla de procesamiento
                                paymentState = PaymentState.PROCESSING

                                // Pago en efectivo - guardar directamente
                                val serviceIds = items.filterIsInstance<ServiceData>().mapNotNull { it.id }
                                val promoIds = items.filterIsInstance<PromotionData>().mapNotNull { it.id }
                                val servicePrice = items.filterIsInstance<ServiceData>().sumOf { it.priceService ?: 0 }
                                val promotionPrice = items.filterIsInstance<PromotionData>().sumOf { it.pricePromotion ?: 0 }

                                val appointment = AppointmentClientData(
                                    id = null,
                                    idClient = userId,
                                    idEmployee = selectedBarber!!,
                                    serviceId = serviceIds,
                                    idPromotion = promoIds,
                                    dateAppointment = selectedDate.toString(),
                                    timeAppointment = requestedStart.format(dbFormatter),
                                    methodPayment = selectedPayment,
                                    totalPrice = servicePrice + promotionPrice,
                                    statusAppointment = 1,
                                    statusPayment = 1,
                                    durationTotal = totalDuration
                                )

                                val appointmentId = withContext(Dispatchers.IO) {
                                    FirebaseRepository.saveAppointment(appointment)
                                }

                                if (appointmentId != null) {
                                    val nameBarber = barbers.firstOrNull { it.id == selectedBarber }?.nameUser ?: ""

                                    // Generar QR con el ID de la cita
                                    val qrBitmap = withContext(Dispatchers.IO) {
                                        generateQRCode(appointmentId)
                                    }

                                    // Enviar correo con QR
                                    withContext(Dispatchers.IO) {
                                        sendEmailWithQR(
                                            to = user.correoUser,
                                            subject = "Confirmación de cita - FadeBarber",
                                            clientName = user.nameUser,
                                            barberName = nameBarber,
                                            date = formatDate(selectedDate.toString()),
                                            time = requestedStart.format(dbFormatter),
                                            qrBitmap = qrBitmap,
                                            fromEmail = "elizaldiromero14@gmail.com",
                                            fromPassword = "rvemlhzgtkzcqjnv"
                                        )
                                    }

                                    occupiedTimeStrings = occupiedTimeStrings + requestedSlots
                                    paymentState = PaymentState.SUCCESS
                                } else {
                                    errorMessage = "Error al guardar la cita. Intenta de nuevo."
                                    paymentState = PaymentState.ERROR
                                }

                                isProcessing = false

                            } else {
                                // Pago con tarjeta - iniciar flujo de Stripe
                                // El estado de procesamiento se manejará en el callback
                                paymentState = PaymentState.PROCESSING
                                iniciarPagoStripe(total)
                            }
                        } catch (e: Exception) {
                            isProcessing = false
                            errorMessage = "Error: ${e.message}"
                            paymentState = PaymentState.ERROR
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFF94A3B8)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Procesando...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        "Confirmar Cita",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}