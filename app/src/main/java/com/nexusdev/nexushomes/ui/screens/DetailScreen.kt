package com.nexusdev.nexushomes.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.nexusdev.nexushomes.model.HouseModel
import com.nexusdev.nexushomes.ui.viewmodel.HomeDataViewModel

@Composable
fun HouseDetailScreen(
    houseId: String, viewModel: HomeDataViewModel, navController: NavHostController
) {
    val context = LocalContext.current
    val houses by viewModel.houses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Al entrar a la pantalla, cargamos la casa por ID
    LaunchedEffect(houseId) {
        viewModel.fetchHouseById(houseId)
    }

    // Si la consulta devuelve un solo elemento
    val house = houses.firstOrNull()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (house == null) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text("No se encontró la propiedad.")
        }
        return
    }

    // ------------------------------------------------
    // UI del detalle
    // ------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // 🔵 Carrusel de fotos
        if (!house.image.isNullOrEmpty()) {
            ImageCarousel(imageUrls = house.image!!)
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // 🔵 Título
            Text(
                text = house.title ?: "Sin título",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // 🔵 Precio
            Text(
                text = "Precio: Q.${house.price ?: "N/A"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // 🔵 Descripción
            Text(
                text = house.description ?: "Sin descripción",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(20.dp))

            // 🔵 Dirección
            Text(
                text = "Dirección:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(house.address ?: "No disponible")

            Spacer(Modifier.height(8.dp))

            // Botón abrir en Google Maps
            Button(
                onClick = {
                    val url =
                        "https://www.google.com/maps/search/?api=1&query=${house.latitude},${house.longitude}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver en Google Maps")
            }

            Spacer(Modifier.height(20.dp))

            // 🔵 Tipo de propiedad
            Text(
                text = "Tipo: ${house.type ?: "No especificado"}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(20.dp))

            // 🔵 Contacto
            Text(
                text = "Contacto:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(house.contact ?: "No disponible")

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween) {

                // Llamar
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${house.contact}")
                        context.startActivity(intent)
                    }, modifier = Modifier.weight(1f)
                ) {
                    Text("Llamar")
                }

                Spacer(Modifier.width(12.dp))

                // WhatsApp
                Button(
                    onClick = {
                        val url = "https://wa.me/${house.contact}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }, modifier = Modifier.weight(1f)
                ) {
                    Text("WhatsApp")
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(imageUrls: List<String>) {

    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    HorizontalPager(
        state = pagerState, modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) { page ->

        AsyncImage(
            model = imageUrls[page],
            contentDescription = "Imagen propiedad",
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )
        PageIndicator(pagerState = pagerState)
    }
}

@Composable
fun PageIndicator(pagerState: PagerState) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {

        repeat(pagerState.pageCount) { iteration ->
            Text(iteration.toString())
        }
    }
}

