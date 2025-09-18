package com.example.fadebarber.navegation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val route: String,
    val label : String,
    val icon: ImageVector
){}