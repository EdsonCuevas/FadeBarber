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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData

@Composable
fun PromotionCard(
    promotion: PromotionData,
    allServices: List<ServiceData>,
    quantity: Int,
    onIncrement: (PromotionData) -> Unit,
    onDecrement: (PromotionData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIncrement(promotion) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = promotion.imagePromotion,
                contentDescription = promotion.namePromotion,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(promotion.namePromotion ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("⏱ ${promotion.durationPromotion ?: 0} min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("Incluye servicios:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                promotion.servicePromotion?.forEach { id ->
                    val serviceName = allServices.find { it.id == id }?.nameService ?: return@forEach
                    Text("- $serviceName", fontSize = 12.sp, color = Color.DarkGray)
                }
                Text("$${promotion.pricePromotion} MXN", fontWeight = FontWeight.Bold)
            }

            if (quantity > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { onDecrement(promotion) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(quantity.toString(), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A1F66))
                            .clickable { onIncrement(promotion) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A1F66))
                        .clickable { onIncrement(promotion) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
