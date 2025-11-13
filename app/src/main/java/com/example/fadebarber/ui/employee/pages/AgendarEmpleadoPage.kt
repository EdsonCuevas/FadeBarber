package com.example.fadebarber.ui.employee.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.HomeTab
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.ui.client.components.EmptyStateCard
import com.example.fadebarber.ui.client.components.PromotionCard
import com.example.fadebarber.ui.client.components.SearchBar
import com.example.fadebarber.ui.client.components.ServiceCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.produceState
import com.example.fadebarber.ui.client.components.AgendaCartForm
import com.example.fadebarber.data.repository.FirebaseRepository
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.res.painterResource
import com.example.fadebarber.R
import com.example.fadebarber.ui.client.pages.CartPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarEmpleadoPage(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    user: UserData
) {
    // Estado y datos
    val info by viewModel.info.collectAsState()
    val services by viewModel.services.collectAsState()
    val promotions by viewModel.promotions.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startRealtimeListeners()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopRealtimeListeners() }
    }

    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Servicios) }
    var searchQuery by remember { mutableStateOf("") }
    var showCart by remember { mutableStateOf(false) }
    var showAgendaSheet by remember { mutableStateOf(false) }
    val barbers by produceState<List<UserData>>(initialValue = emptyList()) {
        value = FirebaseRepository.getBarbers()
    }

    val filteredServices = services.filter { service ->
        searchQuery.isBlank() ||
                (service.nameService ?: "").contains(searchQuery, ignoreCase = true) ||
                (service.descriptionService ?: "").contains(searchQuery, ignoreCase = true)
    }
    val filteredPromos = promotions.filter { promo ->
        searchQuery.isBlank() ||
                (promo.namePromotion ?: "").contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // Header simplificado con carrito y buscador compacto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = info?.barberName ?: "Barbería",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Agenda tu cita",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Carrito con badge
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = cartItems.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showCart = true }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar compacto
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar servicios o promociones",
                    compact = true
                )
            }
        }

        // Contenido principal
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner eliminado

            // Tabs de Servicios / Promociones
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = if (selectedTab == HomeTab.Servicios) 0 else 1,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF1E3A8A),
                            indicator = { tabPositions ->
                                val index = if (selectedTab == HomeTab.Servicios) 0 else 1
                                androidx.compose.material3.TabRowDefaults.Indicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[index])
                                        .height(3.dp),
                                    color = Color(0xFF2563EB)
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == HomeTab.Servicios,
                                onClick = { selectedTab = HomeTab.Servicios },
                                text = { Text("Servicios") },
                                selectedContentColor = Color(0xFF1E3A8A),
                                unselectedContentColor = Color(0xFF64748B)
                            )
                            Tab(
                                selected = selectedTab == HomeTab.Promociones,
                                onClick = { selectedTab = HomeTab.Promociones },
                                text = { Text("Promociones") },
                                selectedContentColor = Color(0xFF1E3A8A),
                                unselectedContentColor = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Contenido por tab
            when (selectedTab) {
                HomeTab.Servicios -> {
                    if (filteredServices.isNotEmpty()) {
                        items(filteredServices) { service ->
                            val qty = cartItems.count { it is ServiceData && it.id == service.id }
                            ServiceCard(
                                service = service,
                                quantity = qty,
                                onIncrement = { s -> viewModel.addToCart(s) },
                                onDecrement = { s -> viewModel.removeFromCart(s) }
                            )
                        }
                    } else {
                        item { EmptyStateCard(message = "No hay servicios disponibles") }
                    }
                }
                HomeTab.Promociones -> {
                    if (filteredPromos.isNotEmpty()) {
                        items(filteredPromos) { promo ->
                            val qty = cartItems.count { it is PromotionData && it.id == promo.id }
                            PromotionCard(
                                promotion = promo,
                                allServices = services,
                                quantity = qty,
                                onIncrement = { p -> viewModel.addToCart(p) },
                                onDecrement = { p -> viewModel.removeFromCart(p) }
                            )
                        }
                    } else {
                        item { EmptyStateCard(message = "No hay promociones disponibles") }
                    }
                }
                else -> { /* No usado en Agenda */ }
            }
        }

        // Botón flotante Agendar
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Button(
                    onClick = { showCart = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calendar_clock),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "AGENDAR CITA",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${cartItems.size} servicio${if (cartItems.size > 1) "s" else ""} seleccionado${if (cartItems.size > 1) "s" else ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_send),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // BottomSheet del carrito
        if (showCart) {
            ModalBottomSheet(
                onDismissRequest = { showCart = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                CartPage(
                    items = cartItems,
                    onClose = { showCart = false },
                    onRemove = { item -> viewModel.removeFromCart(item) },
                    onAdd = { item -> viewModel.addToCart(item) },
                    onAgendar = {
                        // Abrir agenda sobre el carrito
                        showAgendaSheet = true
                    }
                )
            }
        }

        // BottomSheet de Agenda (segundo nivel)
        if (showAgendaSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAgendaSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                AgendaCartForm(
                    items = cartItems,
                    barbers = barbers,
                    userId = user.id,
                    onConfirm = { success, _ ->
                        showAgendaSheet = false
                        showCart = false
                        if (success) {
                            viewModel.clearCart()
                        }
                    },
                    user = user
                )
            }
        }
    }
}