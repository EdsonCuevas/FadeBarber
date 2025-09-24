package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentData
import com.example.fadebarber.ui.employee.components.BlinkingDot
import com.example.fadebarber.ui.employee.components.CardAppointment
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPage(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {

    val scrollState = rememberScrollState()
    var selectedAppointment by remember { mutableStateOf<AppointmentData?>(null) }
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
            color = Color(0xFF0A1F66),
            darkIcons = false
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0A1F66), Color(0xFF2D9CDB)),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        /** HEADER CON IMAGEN Y NOMBRE **/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.perfil),
                contentDescription = "Imagen de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .shadow(shape = RoundedCornerShape(16.dp), elevation = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text("Hola de nuevo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Alfredo", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_start), contentDescription = "Estrella", tint = Color.Unspecified, modifier = Modifier.size(15.dp))
                    Text(text = "4/5", color = Color.White, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        /** CARD PRINCIPAL DE CITA EN CURSO **/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                onClick = { }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cita En Curso", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        BlinkingDot(size = 12.dp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Corte De Cabello", fontSize = 20.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Edson", fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Abrir")
                    }
                }
            }
        }

        /** TITULO CITAS DE HOY **/
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ){
            Text("Citas De Hoy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 20.dp))
        }

        /** LISTADO DE CITAS **/
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            appointments.filter { it.statusAppointment != 0 }.forEach { appointment ->
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
        }

        /** MODAL BOTTOM SHEET **/
        if (selectedAppointment != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = Color.White,
                onDismissRequest = { selectedAppointment = null }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    /** Detalles de la cita seleccionada **/
                    selectedAppointment?.let { appointment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Título del servicio o promoción
                            if (appointment.idPromotion != 0) {
                                promotions.find { it.id == appointment.idPromotion }?.let { promo ->
                                    Text(promo.namePromotion ?: "Sin promoción", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                services.find { it.id == appointment.serviceId }?.let { service ->
                                    Text(service.nameService ?: "Sin servicio", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Hora
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .background(Color(0xFFE6F0FF), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(painter = painterResource(id = R.drawable.ic_reloj), contentDescription = "Hora", tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                Text(appointment.timeAppointment ?: "0:00", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }


                        // Descripción del servicio/promoción
                        if (appointment.idPromotion != 0) {
                            promotions.find { it.id == appointment.idPromotion }?.let { p ->
                                val servicesText = p.servicePromotion?.mapNotNull { serviceId ->
                                    services.find { it.id == serviceId }?.nameService
                                }?.joinToString(", ")
                                Text(servicesText ?: "", fontSize = 14.sp, color = Color.Gray)
                            }
                        } else {
                            services.find { it.id == appointment.serviceId }?.let { s ->
                                Text(s.descriptionService ?: "", fontSize = 14.sp, color = Color.Gray)
                            }
                        }

                        // Datos del cliente
                        val user = users.find { it.id == appointment.idClient }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFF2563EB), RoundedCornerShape(12.dp))
                                .background(Color(0xFFE6F0FF), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Nombre: ${user?.nameUser ?: appointment.nameUser}")
                            Text("Correo: ${user?.correoUser ?: appointment.correoUser}")
                            Text("Teléfono: ${user?.phoneNumberUser ?: appointment.phoneNumberUser}")
                            Text("Fecha: ${formatDate(appointment.dateAppointment) ?: ""}")
                        }

                        // Total del servicio
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE6FAEC), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total del Servicio", color = Color(0xFF16A34A))
                            val totalPrice = if (appointment.idPromotion != 0) {
                                promotions.find { it.id == appointment.idPromotion }?.pricePromotion?.toDouble() ?: 0.0
                            } else {
                                services.find { it.id == appointment.serviceId }?.priceService?.toDouble() ?: 0.0
                            }
                            Text("$ ${"%.2f".format(totalPrice)} MXN", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF16A34A))
                        }

                        // Estado de la cita
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Estado de la Cita", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 8.dp))
                            Text("Activo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.background(Color(0xFFC9FFD1), RoundedCornerShape(12.dp)).padding(12.dp))
                        }

                        // Botones
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3770FD))) { Text("Empezar", color = Color.White) }
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4141))) { Text("Cancelar", color = Color.White) }
                        }

                    }
                }
            }
        }
    }
}
