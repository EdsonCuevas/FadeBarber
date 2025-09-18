package com.example.fadebarber.ui.employee.pages

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.model.Appointment
import com.example.fadebarber.ui.employee.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DashboardPage(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()

) {
    val date by viewModel.date.observeAsState()
    val appointments by viewModel.appointments.observeAsState()
    val scrollState = rememberScrollState()
    var selectedAppointmentId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()


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
                    date?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it.service, fontSize = 20.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it.client, fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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
                appointments?.forEach { appointment ->
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedAppointmentId = appointment.id // asigna el ID de la cita
                            scope.launch { sheetState.show() }

                        }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(appointment.service, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(appointment.hour, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(appointment.client, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(appointment.date, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (selectedAppointmentId != null) {
                    val appointment = appointments?.find { it.id == selectedAppointmentId }

                    ModalBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = {
                            scope.launch {
                                sheetState.hide()
                                if (!sheetState.isVisible) selectedAppointmentId = null
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(16.dp)
                        ) {
                            appointment?.let {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp), // opcional, para que no quede pegado
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Detalle de la cita", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Servicio: ${it.service}")
                                Text("Cliente: ${it.client}")
                                Text("Hora: ${it.hour}")
                                Text("Fecha: ${it.date}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    if (!sheetState.isVisible) selectedAppointmentId = null
                                }
                            }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun BlinkingDot(
    size: Dp = 16.dp,
    color: Color = Color.Green,
    blinkDuration: Int = 500 // milisegundos
) {
    var visible by remember { mutableStateOf(true) }

    // Cambia el estado cada blinkDuration
    LaunchedEffect(Unit) {
        while (true) {
            delay(blinkDuration.toLong())
            visible = !visible
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = color.copy(alpha = if (visible) 1f else 0f),
                shape = CircleShape
            )
    )
}


@Composable
fun Modifier.shimmerLoading(
    colors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.2f)
    ),
    durationMillis: Int = 5000,
    angle: Float = 45f // ángulo diagonal
): Modifier {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    return this.drawBehind {
        val width = size.width
        val height = size.height

        val rad = Math.toRadians(angle.toDouble())
        val xOffset = translateAnim * kotlin.math.cos(rad).toFloat()
        val yOffset = translateAnim * kotlin.math.sin(rad).toFloat()

        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(xOffset, yOffset),
                end = Offset(xOffset + width, yOffset + height)
            ),
            size = size
        )
    }
}



