package com.example.fadebarber.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A66C2))
    ) {
        // Barra de navegación superior
        TopAppBar(
            title = {
                Text(
                    text = "Política de Privacidad",
                    color = Color.White,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Título principal
            Text(
                text = "POLÍTICA DE PRIVACIDAD",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Contenido de política de privacidad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "1. Información que Recopilamos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recopilamos información personal cuando usted se registra en nuestra aplicación, realiza reservas o interactúa con nuestros servicios. Esto puede incluir nombre, dirección de correo electrónico, número de teléfono y datos de ubicación.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "2. Cómo Usamos Su Información",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Utilizamos su información para proporcionar y mejorar nuestros servicios, procesar reservas, enviar notificaciones importantes y personalizar su experiencia en la aplicación.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "3. Compartir Su Información",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No vendemos, comerciamos ni alquilamos su información personal a terceros. Podemos compartir información con proveedores de servicios que nos ayudan a operar nuestra aplicación, realizar negocios o servir a nuestros usuarios, siempre bajo acuerdos de confidencialidad.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "4. Seguridad de la Información",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Implementamos medidas de seguridad adecuadas para proteger contra el acceso no autorizado, alteración, divulgación o destrucción de su información personal.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "5. Cookies",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Utilizamos cookies y tecnologías similares para mejorar la experiencia del usuario, analizar el tráfico y mostrar publicidad personalizada.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "6. Derechos del Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Usted tiene derecho a acceder, corregir, actualizar o eliminar su información personal en cualquier momento. También puede optar por no recibir comunicaciones promocionales.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "7. Cambios a Esta Política",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Podemos actualizar nuestra Política de Privacidad periódicamente. Publicaremos cualquier cambio en esta página y notificaremos a los usuarios si los cambios son significativos.",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "8. Contacto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A66C2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Si tiene preguntas sobre esta Política de Privacidad, por favor contáctenos a través de nuestro soporte en la aplicación o envíenos un correo electrónico a privacidad@fadebarber.com",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}