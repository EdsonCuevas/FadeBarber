package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.employee.components.BlinkingDot
import com.example.fadebarber.ui.employee.components.CardAppointment
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
            color = Color(0xFF0A1F66),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3B5BA7)),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                    Text(
                        "Hola, ${user.nameUser}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_start),
                            contentDescription = "Estrella",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "4/5",
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
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
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                onClick = { }
            ) {
                val hasOngoingAppointment = appointments?.any { it.statusAppointment == 2 } == true

                if (hasOngoingAppointment) {
                    appointments?.forEach { appointment ->
                        if (appointment.statusAppointment == 2) {
                            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cita En Curso", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    BlinkingDot(size = 12.dp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                services?.forEach { service ->
                                    if (appointment.serviceId.contains(service.id)) {
                                        Text(service.nameService ?: "Sin servicio", fontSize = 16.sp, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                users?.forEach { user ->
                                    if (appointment.idClient == user.id) {
                                        Text(user.nameUser ?: "Sin cliente", fontSize = 16.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Solo se muestra una vez si no hay citas
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("No hay citas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            BlinkingDot(size = 12.dp)
                        }
                    }
                }


            }
        }

        /** TITULO CITAS DE HOY **/
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ){
            Text("Citas De Hoy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 20.dp))
        }

        /** LISTADO DE CITAS **/
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if(appointments.isNotEmpty()) {
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
            } else {
                Text("No hay citas disponibles", fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                            if (appointment.idPromotion.isNotEmpty()) {
                                promotions?.forEach { promotion ->
                                    if(appointment.idPromotion.contains(promotion.id)){
                                        Text(promotion.namePromotion ?: "Sin promoción", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                services?.forEach { service ->
                                    if(appointment.idPromotion.contains(service.id)){
                                        Text(service.nameService ?: "Sin servicio", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
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
                        if (appointment.idPromotion.isNotEmpty()) {
                            promotions?.forEach { promotion ->
                                val servicesText = promotion.servicePromotion?.mapNotNull { serviceId ->
                                    services.find { it.id == serviceId }?.nameService
                                }?.joinToString(", ")
                                Text(servicesText ?: "", fontSize = 14.sp, color = Color.Gray)
                            }
                        } else {
                            services?.forEach { service ->
                                if(appointment.serviceId.contains(service.id)) {
                                    Text(
                                        service.descriptionService ?: "",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
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
                           /* Text("Nombre: ${user?.nameUser ?: appointment.nameUser}")
                            Text("Correo: ${user?.correoUser ?: appointment.correoUser}")
                            Text("Teléfono: ${user?.phoneNumberUser ?: appointment.phoneNumberUser}")
                            Text("Fecha: ${formatDate(appointment.dateAppointment) ?: ""}")*/
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

                            selectedAppointment?.let { selectedAppointment ->
                                Text("$ ${selectedAppointment.totalPrice} MXN", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF16A34A))
                            }
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
