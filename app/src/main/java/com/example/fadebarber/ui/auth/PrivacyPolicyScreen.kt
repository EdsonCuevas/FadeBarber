package com.example.fadebarber.ui.auth

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Política de Privacidad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // CONTENIDO SCROLLEABLE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // HERO SECTION
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Color(0xFF2563EB).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Tu privacidad es nuestra prioridad",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "En Fade Barber nos comprometemos a proteger y respetar tu información personal",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Última actualización: Enero 2025",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SECCIÓN 1 - Card destacada
                PrivacySectionCard(
                    icon = Icons.Default.Info,
                    title = "Información que Recopilamos",
                    content = "Recopilamos información personal cuando te registras, realizas reservas o interactúas con nuestros servicios:",
                    items = listOf(
                        "Nombre completo",
                        "Dirección de correo electrónico",
                        "Número de teléfono",
                        "Datos de ubicación para servicios cercanos"
                    )
                )

                // SECCIÓN 2
                PrivacySectionSimple(
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF10B981),
                    title = "Cómo Usamos Tu Información",
                    content = "Tu información nos permite brindarte la mejor experiencia posible. La utilizamos para procesar tus reservas, enviarte notificaciones importantes, mejorar continuamente nuestros servicios y personalizar tu experiencia en la app."
                )

                // SECCIÓN 3
                PrivacySectionCard(
                    icon = Icons.Default.Share,
                    title = "Compartir Información",
                    content = "Tu confianza es fundamental para nosotros:",
                    items = listOf(
                        "NO vendemos tu información personal",
                        "NO comercializamos tus datos",
                        "Solo compartimos con proveedores bajo acuerdos de confidencialidad",
                        "Compartimos solo lo necesario para operar el servicio"
                    )
                )

                // SECCIÓN 4
                PrivacySectionHighlight(
                    icon = Icons.Default.Security,
                    title = "Seguridad de la Información",
                    description = "Implementamos las medidas de seguridad más avanzadas para proteger tu información contra accesos no autorizados, alteraciones o destrucción de datos.",
                    highlightColor = Color(0xFF2563EB)
                )

                // SECCIÓN 5
                PrivacySectionSimple(
                    icon = Icons.Default.Cookie,
                    iconColor = Color(0xFFF59E0B),
                    title = "Uso de Cookies",
                    content = "Utilizamos cookies y tecnologías similares para mejorar tu experiencia, analizar el tráfico de la aplicación y personalizar el contenido que ves."
                )

                // SECCIÓN 6
                PrivacySectionCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "Tus Derechos como Usuario",
                    content = "Tienes control total sobre tu información:",
                    items = listOf(
                        "Acceder a tu información en cualquier momento",
                        "Corregir o actualizar tus datos",
                        "Eliminar tu cuenta y datos asociados",
                        "Optar por no recibir comunicaciones promocionales"
                    )
                )

                // SECCIÓN 7
                PrivacySectionSimple(
                    icon = Icons.Default.Update,
                    iconColor = Color(0xFF8B5CF6),
                    title = "Actualizaciones de Política",
                    content = "Podemos actualizar esta política periódicamente. Te notificaremos sobre cambios significativos y publicaremos las actualizaciones en esta página."
                )

                // SECCIÓN 8 - Contacto destacado
                PrivacySectionHighlight(
                    icon = Icons.Default.Email,
                    title = "¿Tienes Dudas?",
                    description = "Estamos aquí para ayudarte. Contáctanos a través del soporte en la app o envíanos un email a:",
                    highlightText = "privacidad@fadebarber.com",
                    highlightColor = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© 2025 Fade Barber",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
fun PrivacySectionCard(
    icon: ImageVector,
    title: String,
    content: String,
    items: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFF2563EB).copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Text(
                text = content,
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .background(Color(0xFF2563EB), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacySectionSimple(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        iconColor.copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PrivacySectionHighlight(
    icon: ImageVector,
    title: String,
    description: String,
    highlightText: String? = null,
    highlightColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(highlightColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = highlightColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp
            )

            highlightText?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = highlightColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}