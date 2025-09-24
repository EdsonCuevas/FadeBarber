package com.example.fadebarber.ui.client.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fadebarber.R
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.HomeTab
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.components.AgendaPromoForm
import com.example.fadebarber.ui.client.components.AgendaServiceForm
import com.example.fadebarber.ui.client.components.BarberBanner
import com.example.fadebarber.ui.client.components.PromotionCard
import com.example.fadebarber.ui.client.components.ServiceCard
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel(), user: UserData) {

    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Servicios) }

    val info by viewModel.info.collectAsState()
    val services by viewModel.services.collectAsState() // aquí llegan de Firebase
    val promotions by viewModel.promotions.collectAsState()


    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedService by remember { mutableStateOf<ServiceData?>(null) }
    var selectedPromotion by remember { mutableStateOf<PromotionData?>(null) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        // Barra superior con buscador y carrito
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F1F1))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Search", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { print("XD") }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Banner
        BarberBanner(info?.barberBanner)

        Spacer(Modifier.height(16.dp))

        // Nombre barbería y dirección
        Text(info?.barberName.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Avenida Miguel De La Madrid No.234, La Jaras",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // Tabs (simple)
        TabRow(
            selectedTabIndex = when (selectedTab) {
                is HomeTab.Servicios -> 0
                is HomeTab.Combos -> 1
                is HomeTab.Nosotros -> 2
            },
            containerColor = Color(0xFFFFFFFF),
            contentColor = Color(0xFF0A1F66),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(
                        tabPositions[
                            when (selectedTab) {
                                is HomeTab.Servicios -> 0
                                is HomeTab.Combos -> 1
                                is HomeTab.Nosotros -> 2
                            }
                        ]
                    ),
                    color = Color(0xFF0A1F66), // 🔹 azul oscuro
                    height = 3.dp              // puedes ajustar el grosor
                )
            }
        ) {
            listOf(HomeTab.Servicios, HomeTab.Combos, HomeTab.Nosotros).forEach { tab ->
                Tab(
                    selected = selectedTab::class == tab::class,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            is HomeTab.Servicios -> {
                Spacer(Modifier.height(12.dp))
                if(services.isNotEmpty()) {
                    services.forEach { service ->
                        if (service.statusService == 1) {
                            ServiceCard(
                                service = service,
                                onClick = {
                                    selectedService = it
                                    scope.launch { sheetState.show() }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay servicios en este momento",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }

            is HomeTab.Combos -> {
                if(promotions.isNotEmpty()) {
                    promotions.forEach { promo ->
                        PromotionCard(promotion = promo, allServices = services) {
                            selectedPromotion = promo
                            scope.launch { sheetState.show() }

                        }
                        Spacer(Modifier.height(12.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay promociones en este momento",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }


            is HomeTab.Nosotros -> {
                Text("Nosotros", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Aquí información de la barbería: historia, equipo, etc.")
            }
        }

    }

    // BottomSheet de agendar cita con promoción
    if (selectedPromotion != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        selectedPromotion = null
                    }
                }
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AgendaPromoForm(
                promotion = selectedPromotion!!,
                onConfirm = { barbero, fecha, hora ->
                    println("Agendado promo: ${selectedPromotion!!} con $barbero el $fecha a las $hora")
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedPromotion = null
                    }
                }
            )
        }
    }


    // BottomSheet de agendar cita
    if (selectedService != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        selectedService = null
                    }
                }
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            AgendaServiceForm(
                service = selectedService!!,
                onConfirm = { barbero, fecha, hora ->
                    // Aquí iría la lógica de guardar la cita en Firebase, etc.
                    println("Agendado: ${selectedService!!} con $barbero el $fecha a las $hora")
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedService = null
                    }
                }
            )
        }
    }

}