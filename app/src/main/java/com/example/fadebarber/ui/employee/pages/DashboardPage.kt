package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.fadebarber.ui.employee.components.CardAppointment
import com.example.fadebarber.ui.employee.components.CurrentAppointmentCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPage(
    modifier: Modifier = Modifier,
    user: UserData,
    viewModel: DashboardViewModel = viewModel()
) {

    val scrollState = rememberScrollState()
    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Colección de Flows del ViewModel
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val services by viewModel.services.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val promotions by viewModel.promotions.collectAsState(initial = emptyList())

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
            ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        /** HEADER CON IMAGEN Y NOMBRE **/
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
            // SALUDO + PUNTUACIÓN + PERFIL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hola, ${user.nameUser}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Estrellas de puntuación
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_start),
                                contentDescription = "Estrella llena",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_start), // Estrella vacía
                            contentDescription = "Estrella vacía",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "4.0/5",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Imagen de perfil
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // CARD DE PRÓXIMA CITA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = null // Elimina cualquier borde
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Próxima cita",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "15:30 - Juan Pérez",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Icono de calendario
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_reloj), // Necesitarás este icono
                            contentDescription = "Calendario",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }


        /** CARD PRINCIPAL DE CITA EN CURSO **/

        CurrentAppointmentCard(
            appointments = appointments,
            users = users,
            selectedAppointment = selectedAppointment,
            user = user,
            scope = scope,
            onAppointmentClick = {
                selectedAppointment = it
                scope.launch { sheetState.show() }
            }
        )


        /** TITULO CITAS PENDIENTES **/

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top
            ){
                Text("Citas Pendientes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 20.dp))
            }

            /** LISTADO DE CITAS **/
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val userAppointments = appointments
                .filter { it.statusAppointment == 1 && it.idEmployee == user.id }

            if (userAppointments.isNotEmpty()) {
                userAppointments.forEach { appointment ->
                    CardAppointment(
                        appointment = appointment,
                        services = services,
                        users = users,
                        promotions = promotions,
                        onAppointmentClick = {
                            selectedAppointment = it
                            scope.launch { sheetState.show() }
                        }
                    )
                }
            } else {
                // Cuando no hay citas
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_clock),
                    contentDescription = "Calendario",
                    tint = Color(0xFF2563EB), // color azul visible sobre fondo blanco
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "No hay citas disponibles",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB) // mismo color que el icono
                )
            }
        }




        /** MODAL BOTTOM SHEET **/
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
                                        Color(0xFFDCFCE7),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF22C55E), CircleShape)
                                )
                                Text(
                                    text = "Activo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF16A34A)
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
                                            text = "con ${user?.nameUser ?: "Profesional"}",
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
                                            text = "${appointment.totalPrice ?: "0"}.00",
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
                                            text = "60 min",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Servicios a Realizar",
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
                                                    color = Color(0xFF7C3AED)
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

                        // Botones de Acción
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón Empezar
                            Button(
                                onClick = { /* Acción empezar */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B82F6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_user),
                                        contentDescription = "Empezar",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Empezar",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Botón Cancelar
                            OutlinedButton(
                                onClick = { /* Acción cancelar */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_user),
                                        contentDescription = "Cancelar",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Cancelar",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFEF4444)
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
}
