package com.example.fadebarber.navegation

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.selects.select


@Composable
fun BottomBar(
    navController: NavController,
    items: List<NavigationItem>
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    NavigationBar(
        tonalElevation = 12.dp,
        containerColor = Color(0xFF1E3A8A)

    ) {
        items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        if(currentRoute != item.route){
                            navController.navigate(item.route){
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    label = { Text(item.label, fontWeight = FontWeight.Bold) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E3A8A),      // Color del icono cuando está seleccionado
                    unselectedIconColor = Color.White,   // Color del icono no seleccionado
                    selectedTextColor = Color.White,      // Color del label cuando está seleccionado
                    unselectedTextColor = Color.White,    // Color del label no seleccionado
                    indicatorColor = Color.White // Color del fondo del item seleccionado
                    )
                )


        }
    }
}