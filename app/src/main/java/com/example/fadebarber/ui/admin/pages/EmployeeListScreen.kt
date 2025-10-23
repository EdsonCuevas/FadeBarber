package com.example.fadebarber.ui.admin.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.model.AppointmentClientData
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.admin.viewmodel.AdminEmployeeListViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import com.example.fadebarber.ui.employee.pages.AppointmentCard


@Composable
fun EmployeeListScreen() {
    val vm: AdminEmployeeListViewModel = viewModel()
    val barbers by vm.barbers.collectAsState()
    val services by vm.services.collectAsState()
    val promotions by vm.promotions.collectAsState()
    val users by vm.users.collectAsState()

    LaunchedEffect(Unit) { vm.startListeners() }
    DisposableEffect(Unit) {
        onDispose { vm.stopListeners() }
    }

    var expandedBarberId by remember { mutableStateOf<String?>(null) }

    // Derivados
    val todayAppointments = remember(barbers, users) { vm.todayAppointments() }
    val onlineCount = remember(barbers) {
        barbers.count { vm.isUserOnline(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFF), Color(0xFFF0F4FF))
                )
            )
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Empleados",
                fontSize = 22.sp,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.SemiBold
            )
        }

        // Estadísticas (con íconos)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarToday,
                number = todayAppointments.size.toString(),
                label = "Citas hoy"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                number = onlineCount.toString(),
                label = "Activos en línea"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de empleados
        val sortedBarbers = remember(barbers) {
            barbers.sortedWith(compareByDescending<UserData> { vm.isUserOnline(it.id) }
                .thenBy { it.nameUser.lowercase() })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedBarbers, key = { it.id }) { user ->
                val (completed, total) = vm.todayTotalsForEmployee(user.id)
                val apptsForEmployee = vm.appointmentsForEmployeeToday(user.id)
                EmployeeCard(
                    user = user,
                    isOnline = vm.isUserOnline(user.id),
                    completedAppointments = completed,
                    totalAppointments = total,
                    isExpanded = expandedBarberId == user.id,
                    appointments = apptsForEmployee,
                    services = services,
                    promotions = promotions,
                    users = users,
                    onToggleExpand = {
                        expandedBarberId = if (expandedBarberId == user.id) null else user.id
                    }
                )
            }
        }
    }

    // Sheet eliminado: ahora las citas se despliegan dentro de cada tarjeta

}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    number: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color(0xFF0D47A1)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = number,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444)
                )
            }
        }
    }
}

@Composable
fun EmployeeCard(
    user: UserData,
    isOnline: Boolean,
    completedAppointments: Int,
    totalAppointments: Int,
    isExpanded: Boolean,
    appointments: List<AppointmentClientData>,
    services: List<ServiceData>,
    promotions: List<PromotionData>,
    users: List<UserData>,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // FOTO DE PERFIL
                    Image(
                        painter = painterResource(id = R.drawable.profilelogo),
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.nameUser, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFF9CA3AF))
                            )
                        }
                        Text("Barbero", fontSize = 13.sp, color = Color(0xFF666666))
                    }

                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Ocultar citas" else "Ver citas",
                            tint = Color(0xFF374151)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        if (appointments.isEmpty()) {
                            Text("Sin citas para hoy", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            appointments.forEach { appt ->
                                AppointmentCard(
                                    appointment = appt,
                                    services = services,
                                    users = users,
                                    promotions = promotions,
                                    onClick = { /* sin acción en admin */ },
                                    modifier = Modifier.height(92.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            // Texto de citas en la esquina superior derecha
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = "$completedAppointments/$totalAppointments citas",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeAppointmentsSheet(
    employee: UserData,
    appointments: List<AppointmentClientData>,
    services: List<ServiceData>,
    promotions: List<PromotionData>,
    users: List<UserData>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Citas de ${employee.nameUser} (hoy)",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (appointments.isEmpty()) {
                Text("Sin citas para hoy", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appointments, key = { it.id ?: "" }) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            services = services,
                            users = users,
                            promotions = promotions,
                            onClick = { /* sin acción en admin */ },
                            modifier = Modifier.height(92.dp)
                        )
                    }
                }
            }
        }
    }
}
