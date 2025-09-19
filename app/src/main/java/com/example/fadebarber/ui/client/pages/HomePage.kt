package com.example.fadebarber.ui.client.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fadebarber.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fadebarber.data.HomeViewModel
import com.example.fadebarber.data.model.ServiceData
import coil.compose.AsyncImage

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel()) {

    val services by viewModel.services.collectAsState() // aquí llegan de Firebase

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedService by remember { mutableStateOf<ServiceData?>(null) }

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
        Image(
            painter = painterResource(id = R.drawable.perfil), // pon tu imagen aquí
            contentDescription = "Banner barbería",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(16.dp))

        // Nombre barbería y dirección
        Text("BARBER SHOP", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Avenida Miguel De La Madrid No.234, La Jaras",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // Tabs (simple)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Servicios", fontWeight = FontWeight.Bold)
            Text("Combos", color = Color.Gray)
            Text("Nosotros", color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        // Sección servicios
        Text("Servicios", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        // Tarjeta Servicio 1
        services.forEach { service ->
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
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {

        }
    }

}

@Composable
fun ServiceCard(service: ServiceData, onClick: (ServiceData) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(service) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = service.imageService,
                contentDescription = service.nameService,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(service.nameService ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(service.descriptionService ?: "", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("⏱ ${service.durationService ?: 0} min", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("$${service.priceService ?: 0} USD", fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A1F66)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
