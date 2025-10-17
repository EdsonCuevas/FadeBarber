package com.example.fadebarber.ui.client.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fadebarber.data.model.ServiceData


@Composable
fun ServiceCard(service: ServiceData, onClick: (ServiceData) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(service) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del servicio con mejor diseño
            AsyncImage(
                model = service.imageService,
                contentDescription = "Imagen del servicio ${service.nameService}",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            // Contenido principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del servicio
                Text(
                    text = service.nameService ?: "Servicio",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(4.dp))

                // Descripción
                Text(
                    text = service.descriptionService ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                // Información adicional en fila
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duración
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Duración",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF666666)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${service.durationService ?: 0} min",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Precio
                    Text(
                        text = "$${service.priceService} MXN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0A66C2)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Botón de acción mejorado
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A66C2))
                    .clickable { onClick(service) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}