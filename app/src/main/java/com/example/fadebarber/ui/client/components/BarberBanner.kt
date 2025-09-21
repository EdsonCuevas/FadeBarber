package com.example.fadebarber.ui.client.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.valentinilk.shimmer.shimmer

@Composable
fun BarberBanner(infoBanner: String?) {
    SubcomposeAsyncImage(
        model = infoBanner,
        contentDescription = "Banner barbería",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        when (painter.state) {
            is coil.compose.AsyncImagePainter.State.Loading,
            is coil.compose.AsyncImagePainter.State.Error -> {
                // shimmer mientras carga
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer() // efecto shimmer
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}
