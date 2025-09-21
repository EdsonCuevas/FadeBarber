package com.example.fadebarber.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun BlinkingDot(
    size: Dp = 16.dp,
    color: Color = Color.Green,
    blinkDuration: Int = 500 // milisegundos
) {
    var visible by remember { mutableStateOf(true) }

    // Cambia el estado cada blinkDuration
    LaunchedEffect(Unit) {
        while (true) {
            delay(blinkDuration.toLong())
            visible = !visible
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = color.copy(alpha = if (visible) 1f else 0f),
                shape = CircleShape
            )
    )
}