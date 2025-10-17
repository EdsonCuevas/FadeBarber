package com.example.fadebarber.ui.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fadebarber.ui.admin.pages.Employee
import kotlinx.coroutines.launch

// Reutilizamos AppointmentRequest (definido en este mismo paquete)
data class AppointmentRequest(
    val id: Int,
    val clientName: String,
    val employeeName: String,
    val service: String,
    val time: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeModalSheet(
    employee: Employee?,
    onDismiss: () -> Unit
) {
    if (employee == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            Text(
                text = "Clientes de ${employee.name}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (employee.clients.isEmpty()) {
                Text("Sin clientes asignados", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(employee.clients) { client ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(client.clientName, fontWeight = FontWeight.Medium)
                                    Text("${client.service} • ${client.time}", color = Color.Gray)
                                }

                                // Badge de estado simple
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (client.status) {
                                        "Aceptado" -> Color(0xFFD4EDDA)
                                        "En curso" -> Color(0xFFFFF3CD)
                                        else -> Color(0xFFF8D7DA)
                                    }
                                ) {
                                    Text(
                                        client.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar")
            }
        }
    }
}
