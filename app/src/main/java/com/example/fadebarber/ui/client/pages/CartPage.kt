package com.example.fadebarber.ui.client.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.fadebarber.R
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData

@Composable
fun CartPage(
    items: List<Any>,
    onClose: () -> Unit,
    onRemove: (Any) -> Unit,
    onAgendar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 🔹 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Carrito de compras", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClose) {
                Text("Cerrar ✕")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío 🛒", color = Color.Gray)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 🔹 Izquierda: Imagen + info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = when (item) {
                                        is ServiceData -> item.imageService
                                        is PromotionData -> item.imagePromotion
                                        else -> R.drawable.loading
                                    },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = when (item) {
                                            is ServiceData -> item.nameService
                                            is PromotionData -> item.namePromotion
                                            else -> ""
                                        }.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = when (item) {
                                            is ServiceData -> "⏱ " + item.durationService + " min"
                                            is PromotionData -> "⏱ " + item.durationPromotion + " min"
                                            else -> ""
                                        },
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }

                            // 🔹 Derecha: precio/descuento + eliminar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (item) {
                                        is ServiceData -> "${item.priceService} MXN"
                                        is PromotionData -> "${item.pricePromotion} MXN"
                                        else -> ""
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onRemove(item) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // 🔹 Botón para agendar
            androidx.compose.material3.Button(
                onClick = onAgendar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A66C2)
                )
            ) {
                Text("Agendar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
