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
import com.example.fadebarber.R
import com.example.fadebarber.ui.admin.components.AppointmentRequest
import com.example.fadebarber.ui.admin.components.EmployeeModalSheet

// MODELO local (ficticio)
data class Employee(
    val id: Int,
    val name: String,
    val specialty: String,
    val completedAppointments: Int,
    val totalAppointments: Int,
    val clients: List<AppointmentRequest>
)

@Composable
fun EmployeeListScreen() {
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
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
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Empleados",
                fontSize = 22.sp,
                color = Color.White,
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
                number = "40",
                label = "Citas"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.People,
                number = "4",
                label = "Empleados"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de empleados (sin mostrar clientes aquí)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleEmployees()) { employee ->
                EmployeeCard(
                    employee = employee,
                    onClick = { selectedEmployee = employee }
                )
            }
        }
    }

    // Bottom sheet: clientes del empleado seleccionado
    EmployeeModalSheet(
        employee = selectedEmployee,
        onDismiss = { selectedEmployee = null }
    )
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
fun EmployeeCard(employee: Employee, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        Text(employee.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(employee.specialty, fontSize = 13.sp, color = Color(0xFF666666))
                    }
                }
            }

            // Texto de citas en la esquina superior derecha
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd) // esquina superior derecha
                    .padding(8.dp)
            ) {
                Text(
                    text = "${employee.completedAppointments}/${employee.totalAppointments} citas",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}


// Datos ficticios para probar
private fun sampleEmployees() = listOf(
    Employee(
        id = 1,
        name = "Edson Felix",
        specialty = "Barbero",
        completedAppointments = 3,
        totalAppointments = 7,
        clients = listOf(
            AppointmentRequest(1, "Alfredo Elizaldi", "Edson Felix", "Corte Barba", "10:00 AM", "Aceptado"),
            AppointmentRequest(2, "Ricardo Cabanilla", "Edson Felix", "Corte Barba", "12:00 PM", "En Proceso")
        )
    ),
    Employee(
        id = 2,
        name = "Gabriel Valencia",
        specialty = "Barbero",
        completedAppointments = 3,
        totalAppointments = 5,
        clients = listOf(
            AppointmentRequest(3, "Pedro Martínez", "Gabriel Valencia", "Corte Cabello", "2:00 PM", "Pendiente")
        )
    )
)
