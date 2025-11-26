package com.nexusdev.nexushomes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.nexusdev.nexushomes.R
import com.nexusdev.nexushomes.ui.components.HouseCard
import com.nexusdev.nexushomes.ui.viewmodel.HomeDataViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val viewModel: HomeDataViewModel = viewModel()

    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    // 📌 Observamos las casas desde el ViewModel
    val houses by viewModel.houses.collectAsState()

    // 📌 Estado de búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // 📌 Filtrado dinámico
    val filteredHouses = houses.filter { house ->
        val query = searchQuery.lowercase()
        house.title?.lowercase()?.contains(query) == true ||
                house.address?.lowercase()?.contains(query) == true
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHomes()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {

        // Header — sin cambios
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nexus Homes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Image(
                    painter = if (isDarkTheme)
                        painterResource(R.drawable.outline_settings_ligth)
                    else painterResource(R.drawable.outline_settings_dark),
                    contentDescription = "settings",
                    modifier = Modifier
                        .height(24.dp)
                        .clickable {
                            Toast.makeText(context, "Settings", Toast.LENGTH_SHORT).show()
                        }
                )
            }
        }


        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = if (isDarkTheme) {
                        painterResource(id = R.drawable.outline_upload_ligth)
                    } else {
                        painterResource(id = R.drawable.outline_upload_dark)
                    },
                    contentDescription = "upload new icon",
                    modifier = Modifier
                        .height(24.dp)
                        .clickable(onClick = { navController.navigate("addNew") })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicar", modifier = Modifier.clickable {
                    navController.navigate("addNew")
                })
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {

            // --------------------------------
            // 🔍 SearchBar
            // --------------------------------
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por título o dirección...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                maxLines = 1,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------
            // 🏡 GRID DE PROPIEDADES (2 columnas)
            // --------------------------------
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredHouses) { house ->
                    HouseCard(
                        house = house,
                        onClick = {
                            navController.navigate("detail/${house.id}")
                        }
                    )
                }
            }
        }
    }
}
