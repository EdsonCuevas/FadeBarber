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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.fadebarber.data.repository.FirebaseRepository
import com.example.fadebarber.ui.client.components.AgendaCartForm
import com.example.fadebarber.ui.client.components.BarberBanner
import com.example.fadebarber.ui.client.components.PromotionCard
import com.example.fadebarber.ui.client.components.SearchBar
import com.example.fadebarber.ui.client.components.ServiceCard
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    user: UserData,
) {

    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Servicios) }

    val info by viewModel.info.collectAsState()
    val services by viewModel.services.collectAsState()
    val promotions by viewModel.promotions.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState() // ✅ Cambio aquí

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
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_reloj),
                                    contentDescription = "Inicio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = info?.barberName ?: "Barbería",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Hola, ${user.nameUser}",
                                    fontSize = 14.sp,
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
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Search Bar
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Buscar servicios o promociones"
                    )
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

                // Tabs
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = when (selectedTab) {
                                is HomeTab.Servicios -> 0
                                is HomeTab.Promociones -> 1
                                is HomeTab.Nosotros -> 2
                            },
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF2563EB),
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(
                                        tabPositions[
                                            when (selectedTab) {
                                                is HomeTab.Servicios -> 0
                                                is HomeTab.Promociones -> 1
                                                is HomeTab.Nosotros -> 2
                                            }
                                        ]
                                    ),
                                    color = Color(0xFF2563EB),
                                    height = 3.dp
                                )
                            }
                        ) {
                            listOf(
                                HomeTab.Servicios,
                                HomeTab.Promociones,
                                HomeTab.Nosotros
                            ).forEach { tab ->
                                Tab(
                                    selected = selectedTab::class == tab::class,
                                    onClick = { selectedTab = tab },
                                    text = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (selectedTab::class == tab::class)
                                                FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Contenido según tab seleccionado
                when (selectedTab) {
                    is HomeTab.Servicios -> {
                        if (filteredServices.isNotEmpty()) {
                            items(filteredServices) { service ->
                                ServiceCard(
                                    service = service,
                                    onClick = {
                                        viewModel.addToCart(service) // ✅ Cambio aquí
                                        alertMessage = "${service.nameService} agregado al carrito 🛒"
                                        alertColor = Color(0xFF10B981)
                                        showAlert = true
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
                                PromotionCard(promotion = promo, allServices = services) {
                                    viewModel.addToCart(promo) // ✅ Cambio aquí
                                    alertMessage = "${promo.namePromotion} agregado al carrito 🛒"
                                    alertColor = Color(0xFF10B981)
                                    showAlert = true
                                }
                            }
                        } else {
                            item {
                                EmptyStateCard(message = "No hay promociones disponibles")
                            }
                        }
                    }

                    is HomeTab.Nosotros -> {
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
                                    // Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    Color(0xFF2563EB).copy(alpha = 0.1f),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✂️",
                                                fontSize = 24.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Sobre Nosotros",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Historia
                                    Text(
                                        text = "Nuestra Historia",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Desde 2015, Fade Barber se ha convertido en el referente de estilo masculino en Colima. Comenzamos como un pequeño local con una gran visión: ofrecer cortes de calidad internacional con el toque personal que caracteriza a nuestra ciudad.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Misión
                                    Text(
                                        text = "Nuestra Misión",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Brindar una experiencia única de barbería moderna, donde cada cliente recibe un servicio personalizado de la más alta calidad, utilizando las mejores técnicas y productos del mercado.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

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

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Divider
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Equipo
                                    Text(
                                        text = "Nuestro Equipo",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Contamos con un equipo de barberos profesionales certificados, constantemente capacitados en las últimas tendencias y técnicas de corte. Cada miembro de nuestro equipo está comprometido con la excelencia y la satisfacción del cliente.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Valores
                                    Text(
                                        text = "Nuestros Valores",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    ValueInfoItem(
                                        emoji = "✨",
                                        title = "Calidad",
                                        description = "Utilizamos productos premium y técnicas avanzadas"
                                    )
                                    ValueInfoItem(
                                        emoji = "🤝",
                                        title = "Profesionalismo",
                                        description = "Servicio personalizado y atención al detalle"
                                    )
                                    ValueInfoItem(
                                        emoji = "⏰",
                                        title = "Puntualidad",
                                        description = "Respetamos tu tiempo con citas programadas"
                                    )
                                    ValueInfoItem(
                                        emoji = "💯",
                                        title = "Satisfacción",
                                        description = "Tu conformidad es nuestra prioridad"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BottomSheets
        if (showCartAgenda) {
            ModalBottomSheet(
                onDismissRequest = { showCartAgenda = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                AgendaCartForm(
                    items = cartItems,
                    barbers = barbers,
                    userId = user.id,
                    onConfirm = { success, message ->
                        alertMessage = message
                        alertColor = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                        showAlert = true
                        showCart = false
                        showCartAgenda = false
                        viewModel.clearCart() // ✅ Cambio aquí
                    }
                )
            }
        }

        if (showCart) {
            ModalBottomSheet(
                onDismissRequest = { showCart = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White
            ) {
                CartPage(
                    items = cartItems,
                    onClose = { showCart = false },
                    onRemove = { item -> viewModel.removeFromCart(item) }, // ✅ Cambio aquí
                    onAgendar = {
                        showCartAgenda = true
                    }
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                color = Color(0xFF1E293B)
            )
        }
    }
}