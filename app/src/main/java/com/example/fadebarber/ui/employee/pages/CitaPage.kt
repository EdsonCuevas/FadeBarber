package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.DashboardViewModel
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.ui.employee.components.CardAppointment
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaPage(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var selectedAppointment by remember { mutableStateOf<AppointmentClientData?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }

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

        // HEADER FECHAS
        Text(
            "Selecciona la fecha",
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(days) { day ->
                val isSelected = selectedDate == day
                Card(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { selectedDate = day },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0A66C2) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                        Text(
                            text = day.month.name.take(3),
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
        // TITULO CITAS DE HOY
        Text(
            "Citas De Hoy",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
        )

        // LISTADO DE CITAS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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

    }
}

