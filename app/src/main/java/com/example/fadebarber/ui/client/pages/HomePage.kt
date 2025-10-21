package com.example.fadebarber.ui.client.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.R
import com.example.fadebarber.data.AuthViewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.HomeTab
import com.example.fadebarber.data.model.PromotionData
import com.example.fadebarber.data.model.ServiceData
import com.example.fadebarber.data.model.UserData
import com.example.fadebarber.data.model.BarberInfo
import com.example.fadebarber.data.repository.FirebaseRepository
import com.example.fadebarber.ui.client.components.AgendaCartForm
import com.example.fadebarber.ui.client.components.BarberBanner
import com.example.fadebarber.ui.client.components.SkeletonHorarioItem
import com.example.fadebarber.ui.client.components.InfoStatCard
import com.example.fadebarber.ui.client.components.PromotionCard
import com.example.fadebarber.ui.client.components.SearchBar
import com.example.fadebarber.ui.client.components.ServiceCard
import com.example.fadebarber.ui.client.components.ValueInfoItem
import com.example.fadebarber.ui.client.components.EmptyStateCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    user: UserData,
    navController: androidx.navigation.NavHostController,
) {

    // Iniciar listeners cuando la página se muestre
    LaunchedEffect(Unit) {
        viewModel.startRealtimeListeners()
        viewModel.listenToBarberInfo()
    }

    // Detener listeners cuando la página se destruya
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRealtimeListeners()
            viewModel.stopListeningToBarberInfo()
        }
    }

    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Nosotros) }

    val info by viewModel.info.collectAsState()
    val services by viewModel.services.collectAsState()
    val promotions by viewModel.promotions.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState() // ✅ Cambio aquí
    var showAgendaSheet by remember { mutableStateOf(false) }

    val barbers by produceState<List<UserData>>(initialValue = emptyList()) {
        value = FirebaseRepository.getBarbers()
    }

    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var alertColor by remember { mutableStateOf(Color(0xFF10B981)) }

    var searchQuery by remember { mutableStateOf("") }

    var showCart by remember { mutableStateOf(false) }

    var showCartAgenda by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Status bar color matching CitaPageClient
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = Color(0xFF1E3A8A), darkIcons = false)
    }

    // Filtros
    val filteredServices = services.filter { service ->
        searchQuery.isBlank() ||
                service.nameService!!.contains(searchQuery, ignoreCase = true) ||
                service.descriptionService!!.contains(searchQuery, ignoreCase = true)
    }

    val filteredPromos = promotions.filter { promo ->
        searchQuery.isBlank() ||
                promo.namePromotion!!.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Alerta flotante
        AnimatedVisibility(
            visible = showAlert && alertMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .zIndex(2f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = alertColor),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (alertColor == Color(0xFF10B981))
                                Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alertMessage ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { showAlert = false }) {
                        Text("✕", color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            LaunchedEffect(alertMessage) {
                if (alertMessage != null) {
                    delay(2000L)
                    showAlert = false
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            // HEADER MEJORADO (igual a CitaPageClient)
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
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            /*Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.logo_goku),
                                    contentDescription = "Inicio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))*/
                            Column {
                                Text(
                                    text = info?.barberName ?: "Barbería",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Hola, ${user.nameUser}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
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
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = "Carrito",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    /*Spacer(modifier = Modifier.height(20.dp))

                    // Bienvenida
                    Column {
                        Text(
                            text = "Bienvenido a Fade Barber",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estilo premium, atención personalizada y citas a tu medida.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }*/
                }
            }

            // CONTENIDO CON LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner
                item {
                    BarberBanner(info?.barberBanner)
                }

                // Dirección
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Abrir Google Maps con la ubicación de Universidad de Colima Campus Naranjo
                                val gmmIntentUri =
                                    Uri.parse("geo:0,0?q=Universidad+de+Colima+Campus+Naranjo")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color(0xFF2563EB).copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Ubicación",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Ubicación",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Universidad de Colima Campus Naranjo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_directions),
                                contentDescription = "Abrir en Maps",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Bienvenida / Hero promocional
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
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Bienvenido a Fade Barber",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Estilo premium, atención personalizada y citas a tu medida.",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Contenido según tab seleccionado
                when (selectedTab) {
                    is HomeTab.Servicios -> {
                        if (filteredServices.isNotEmpty()) {
                            items(filteredServices) { service ->
                                val qty = cartItems.count { it is com.example.fadebarber.data.model.ServiceData && (it as com.example.fadebarber.data.model.ServiceData).id == service.id }
                                ServiceCard(
                                    service = service,
                                    quantity = qty,
                                    onIncrement = { s ->
                                        viewModel.addToCart(s)
                                        alertMessage = "${s.nameService} agregado al carrito"
                                        alertColor = Color(0xFF10B981)
                                        showAlert = true
                                    },
                                    onDecrement = { s ->
                                        viewModel.removeFromCart(s)
                                    }
                                )
                            }
                        } else {
                            item {
                                EmptyStateCard(message = "No hay servicios disponibles")
                            }
                        }
                    }

                    is HomeTab.Promociones -> {
                        if (filteredPromos.isNotEmpty()) {
                            items(filteredPromos) { promo ->
                                val qty = cartItems.count { it is com.example.fadebarber.data.model.PromotionData && (it as com.example.fadebarber.data.model.PromotionData).id == promo.id }
                                PromotionCard(
                                    promotion = promo,
                                    allServices = services,
                                    quantity = qty,
                                    onIncrement = { p ->
                                        viewModel.addToCart(p)
                                        alertMessage = "${p.namePromotion} agregado al carrito"
                                        alertColor = Color(0xFF10B981)
                                        showAlert = true
                                    },
                                    onDecrement = { p ->
                                        viewModel.removeFromCart(p)
                                    }
                                )
                            }
                        } else {
                            item {
                                EmptyStateCard(message = "No hay promociones disponibles")
                            }
                        }
                    }

                    is HomeTab.Nosotros -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { navController.navigate("agend") },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(84.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .background(Color(0xFF2563EB).copy(alpha = 0.14f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_calendar_clock),
                                                        contentDescription = "Agendar",
                                                        tint = Color(0xFF2563EB),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Agendar",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                        }
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { navController.navigate("date") },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(84.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFFECFEFF), Color(0xFFE0F2FE))
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .background(Color(0xFF0EA5E9).copy(alpha = 0.14f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = androidx.compose.material.icons.Icons.Filled.ListAlt,
                                                        contentDescription = "Historial De Citas",
                                                        tint = Color(0xFF0EA5E9),
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Historial De Citas",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                                        .padding(20.dp)
                                ) {
                                    // Header compacto
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color(0xFF2563EB).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "✂️", fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Sobre Nosotros", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Text("Calidad, atención y estilo en cada visita.", fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Stats Cards
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        InfoStatCard(
                                            number = "9+",
                                            label = "Años de experiencia",
                                            modifier = Modifier.weight(1f)
                                        )
                                        InfoStatCard(
                                            number = "5K+",
                                            label = "Clientes satisfechos",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        InfoStatCard(
                                            number = "15+",
                                            label = "Servicios premium",
                                            modifier = Modifier.weight(1f)
                                        )
                                        InfoStatCard(
                                            number = "★ 4.9",
                                            label = "Calificación promedio",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Valores (visual, desplazable)
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        items(listOf("Calidad", "Profesionalismo", "Puntualidad", "Satisfacción")) { label ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF334155)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        item {
                            HorariosSection(info = info)
                        }
                    }
                }
            }
        }

        // 🎯 BOTTOMSHEET DEL CARRITO (PRIMER NIVEL)
        if (showCart) {
            ModalBottomSheet(
                onDismissRequest = { showCart = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                CartPage(
                    items = cartItems,
                    onClose = { showCart = false },
                    onRemove = { item ->
                        viewModel.removeFromCart(item)
                        alertMessage = "❌ Servicio eliminado"
                        alertColor = Color(0xFFEF4444)
                        showAlert = true
                    },
                    onAgendar = {
                        // 🎯 Abrir el sheet de agenda SIN cerrar el carrito
                        showAgendaSheet = true
                    }
                )
            }
        }

        // 🎯 BOTTOMSHEET DE AGENDA (SEGUNDO NIVEL - SE ABRE SOBRE EL CARRITO)
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
                    onConfirm = { success, message ->
                        alertMessage = if (success) "✅ $message" else "❌ $message"
                        alertColor = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                        showAlert = true
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

@Composable
fun InfoStatCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2563EB).copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ValueInfoItem(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFF2563EB).copy(alpha = 0.1f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color(0xFF2563EB).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_clock),
                    contentDescription = "Sin contenido",
                    tint = Color(0xFF2563EB).copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explora nuestras otras opciones",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HorariosSection(info: BarberInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = "Horarios",
                        tint = Color(0xFF10B981)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Horarios de atención", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("Consulta disponibilidad por día", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (info == null) {
                Column {
                    repeat(3) {
                        SkeletonHorarioItem()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {
                val diasOrdenados = listOf(
                    "monday" to "Lunes",
                    "tuesday" to "Martes",
                    "wednesday" to "Miércoles",
                    "thursday" to "Jueves",
                    "friday" to "Viernes",
                    "saturday" to "Sábado",
                    "sunday" to "Domingo"
                )
                diasOrdenados.forEach { (key, nombre) ->
                    val day = info.schedule[key]
                    HorarioLinea(
                        diaNombre = nombre,
                        start = day?.start,
                        end = day?.end,
                        abierto = day?.available == true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun HorarioLinea(
    diaNombre: String,
    start: String?,
    end: String?,
    abierto: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (abierto) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = diaNombre.first().toString(),
                color = if (abierto) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = diaNombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            if (abierto) {
                Text(text = "${start ?: "--:--"} a ${end ?: "--:--"}", fontSize = 13.sp, color = Color(0xFF334155))
            } else {
                Text(text = "Cerrado", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        }
        Box(
            modifier = Modifier
                .background(
                    if (abierto) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (abierto) "Abierto" else "Cerrado",
                fontSize = 12.sp,
                color = if (abierto) Color(0xFF065F46) else Color(0xFF7F1D1D)
            )
        }
    }
}
