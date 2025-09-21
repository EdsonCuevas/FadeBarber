package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentData
import com.example.fadebarber.ui.employee.components.BlinkingDot
import com.example.fadebarber.ui.employee.components.CardAppointment
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DashboardPage(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {

    val scrollState = rememberScrollState()
    var selectedAppointment by remember { mutableStateOf<AppointmentData?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()
    val users by viewModel.users.collectAsState()
    val promotion by viewModel.promotion.collectAsState()
    val systemUiController = rememberSystemUiController()

    SideEffect {
        // Opción 1: color sólido
        systemUiController.setStatusBarColor(
            color = Color(0xFF0A1F66), // azul oscuro del gradiente
            darkIcons = false           // íconos blancos
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
        // Fila superior con imagen y textos
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
                    .shadow(shape = RectangleShape, elevation = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp)) // espacio entre imagen y textos

            Column {
                Text("Hola de nuevo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Alfredo", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
        //Fila media donde muestra la card de la cita actual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ,
            verticalAlignment = Alignment.Top
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB) // bg-blue-600
                ),
                onClick = { },
            ) {

                //Card principal de cita en curso
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp) // padding interno
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Text(
                            text = "Cita En Curso",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        //Spacer(modifier = Modifier.width(160.dp)) // separación entre texto y punto
                        BlinkingDot(size = 12.dp)
                    }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Corte De Cabello", fontSize = 20.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Edson", fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))


                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,   // fondo del botón
                            contentColor = Color.Black      // color del texto
                        )
                    ) {
                        Text("Abrir")
                    }
                }
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ){
            //Spacer(modifier = Modifier.width(20.dp))
            Text("Citas De Hoy",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 20.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                appointments.forEach { appointment ->
                    CardAppointment(
                        appointment = appointment,
                        services = services,
                        users = users,
                        promotions = promotion,
                        onAppointmentClick = {
                            selectedAppointment = it
                            scope.launch { sheetState.show() }
                        }
                    )
                }

            }

        }

        if (selectedAppointment != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) selectedAppointment = null
                    }
                }
            ) {
                Text("Detalles de la cita seleccionada: ${selectedAppointment!!.id}")
            }
        }
    }
}








