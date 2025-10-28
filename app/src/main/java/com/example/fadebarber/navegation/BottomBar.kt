package com.example.fadebarber.navegation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavController,
    items: List<NavigationItem>
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0B3DA3), Color(0xFF2563EB))
                    )
                ),
            tonalElevation = 0.dp,
            containerColor = Color.Transparent
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    label = "bottomBarIconScale"
                )

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                    },
                    label = {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(text = item.label, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFFB3C5F5),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.Transparent,
                        indicatorColor = Color(0xFF2563EB).copy(alpha = 0.15f)
                    ),
                    alwaysShowLabel = false
                )
            }
        }
    }
}