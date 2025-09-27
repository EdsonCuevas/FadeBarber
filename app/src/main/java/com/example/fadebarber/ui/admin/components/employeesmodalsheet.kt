package com.example.fadebarber.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.fadebarber.ui.admin.AppointmentRequest
import com.example.fadebarber.ui.admin.AppointmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBottomSheet(
    appointment: AppointmentRequest?,
    onDismiss: () -> Unit,
    onFinalize: () -> Unit,
    onCancel: () -> Unit
) {
    if (appointment == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Detalles de la Cita", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Text("Cliente: ${appointment.clientName}")
            Text("Empleado: ${appointment.employeeName}")
            Text("Servicio: ${appointment.service}")
            Text("Horario: ${appointment.time}")
            Text("Estado: ${appointment.status}")

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (appointment.status == AppointmentStatus.IN_PROCESS ||
                    appointment.status == AppointmentStatus.PENDING
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onFinalize()
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Finalizar")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onCancel()
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
