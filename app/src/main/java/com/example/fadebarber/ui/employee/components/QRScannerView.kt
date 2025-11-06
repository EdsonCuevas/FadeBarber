@file:OptIn(ExperimentalGetImage::class)
package com.example.fadebarber.ui.employee.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.util.concurrent.Executors

@ExperimentalGetImage
private fun processImageProxy(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue
                if (value != null) {
                    onResult(value)
                }
            }
            .addOnFailureListener {
                // Ignorar fallos de frame
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
private fun ScannerOverlay(
    modifier: Modifier = Modifier,
    cornerColor: Color = Color(0xFF2563EB),
    cornerLength: Dp = 32.dp,
    cornerThickness: Dp = 4.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val l = cornerLength.toPx()
        val t = cornerThickness.toPx()

        // top-left
        drawLine(
            color = cornerColor,
            start = Offset(0f, 0f),
            end = Offset(l, 0f),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(0f, 0f),
            end = Offset(0f, l),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        // top-right
        drawLine(
            color = cornerColor,
            start = Offset(w, 0f),
            end = Offset(w - l, 0f),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(w, 0f),
            end = Offset(w, l),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        // bottom-left
        drawLine(
            color = cornerColor,
            start = Offset(0f, h),
            end = Offset(l, h),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(0f, h),
            end = Offset(0f, h - l),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        // bottom-right
        drawLine(
            color = cornerColor,
            start = Offset(w, h),
            end = Offset(w - l, h),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
        drawLine(
            color = cornerColor,
            start = Offset(w, h),
            end = Offset(w, h - l),
            strokeWidth = t,
            cap = StrokeCap.Round
        )
    }
}

// Helper: valida el QR y actualiza estado en Firebase
private fun handleQrScan(
    context: android.content.Context,
    code: String,
    currentEmployeeId: String?,
    onResult: (String) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val appointmentId = code.trim()
    if (appointmentId.isEmpty()) {
        onResult("Error: QR inválido")
        return
    }

    val appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointment")
    appointmentsRef.child(appointmentId)
        .get()
        .addOnSuccessListener(mainExecutor) { snapshot ->
            val appt = snapshot.getValue(com.example.fadebarber.data.model.AppointmentClientData::class.java)
            if (appt == null) {
                onResult("Error: cita no encontrada o datos inválidos")
            } else {
                val todayStr = LocalDate.now().toString()
                val apptDateStr = appt.dateAppointment?.substring(0, 10)
                val qrStat = appt.qrStatus ?: 1

                if (apptDateStr != todayStr) {
                    onResult("Error: QR caducado. Fecha de cita: ${apptDateStr ?: "desconocida"}")
                } else if (qrStat == 2) {
                    onResult("Error: QR ya utilizado")
                } else {
                    // Verificar si hay alguna cita en curso para el empleado actual (hoy)
                    val query = if (!currentEmployeeId.isNullOrBlank()) {
                        appointmentsRef.orderByChild("idEmployee").equalTo(currentEmployeeId)
                    } else {
                        appointmentsRef
                    }
                    query.get()
                        .addOnSuccessListener(mainExecutor) { listSnap ->
                            var runningFound = false
                            val today = LocalDate.now().toString()
                            for (child in listSnap.children) {
                                val other = child.getValue(com.example.fadebarber.data.model.AppointmentClientData::class.java)
                                if (other != null) {
                                    val otherDate = other.dateAppointment?.substring(0, 10)
                                    val isToday = otherDate == today
                                    val isRunning = (other.statusAppointment == 2)
                                    val isDifferent = (other.id != appointmentId)
                                    if (isToday && isRunning && isDifferent) {
                                        runningFound = true
                                        break
                                    }
                                }
                            }

                            if (runningFound) {
                                onResult("Ya hay una cita en curso. No puedes validar otra.")
                            } else {
                                val updates = mapOf(
                                    "statusAppointment" to 2,
                                    "qrStatus" to 2
                                )
                                appointmentsRef.child(appointmentId)
                                    .updateChildren(updates)
                                    .addOnSuccessListener(mainExecutor) {
                                        onResult("Cita validada. Estado actualizado a 'En curso'.")
                                    }
                                    .addOnFailureListener(mainExecutor) { e ->
                                        onResult("Error al actualizar la cita: ${e.message}")
                                    }
                            }
                        }
                        .addOnFailureListener(mainExecutor) { e ->
                            onResult("Error validando estado actual: ${e.message}")
                        }
                }
            }
        }
        .addOnFailureListener(mainExecutor) { e ->
            onResult("Error al consultar cita: ${e.message}")
        }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerView(
    modifier: Modifier = Modifier,
    currentEmployeeId: String? = null,
    onResult: (String) -> Unit,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var processedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onClose?.invoke() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF2563EB)
                )
            }
            Spacer(Modifier.size(8.dp))
            Text("Escanear QR", color = Color(0xFF2563EB))
        }

        if (hasPermission) {
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
            DisposableEffect(Unit) {
                onDispose { cameraExecutor.shutdown() }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(420.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val scanner = BarcodeScanning.getClient(
                                    BarcodeScannerOptions.Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                        .build()
                                )

                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    processImageProxy(imageProxy, scanner) { value ->
                                        if (!processedOnce) {
                                            processedOnce = true
                                            handleQrScan(context, value, currentEmployeeId, onResult)
                                        }
                                    }
                                }

                                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        selector,
                                        preview,
                                        analysis
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )

                    ScannerOverlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(36.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Se requiere permiso de cámara", color = Color(0xFF2563EB))
                Spacer(Modifier.size(8.dp))
                Text("Por favor acepta el permiso del sistema.", color = Color(0xFF2563EB))
            }
        }
    }
}