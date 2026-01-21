package com.nexusdev.nexushomes.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.nexusdev.nexushomes.R
import com.nexusdev.nexushomes.ui.components.ModernHouseCard
import com.nexusdev.nexushomes.ui.viewmodel.HomeDataViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavHostController
) {
    val viewModel: HomeDataViewModel = viewModel()
    val firebaseAuth = FirebaseAuth.getInstance()
    val user = firebaseAuth.currentUser

    // Estados
    val houses by viewModel.houses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }

    // Filtrado dinámico
    val filteredHouses = remember(houses, searchQuery, selectedFilter) {
        var filtered = houses.filter { house ->
            val query = searchQuery.lowercase()
            house.title?.lowercase()?.contains(query) == true ||
                    house.address?.lowercase()?.contains(query) == true ||
                    house.description?.lowercase()?.contains(query) == true
        }

        filtered = when (selectedFilter) {
            1 -> filtered.filter { it.type?.lowercase()?.contains("casa") == true }
            2 -> filtered.filter { it.type?.lowercase()?.contains("habitación") == true }
            3 -> filtered.filter { it.type?.lowercase()?.contains("apartamento") == true }
            else -> filtered
        }
        filtered
    }

    LaunchedEffect(Unit) {
        viewModel.fetchHomes()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (user != null) navController.navigate("profile") else navController.navigate(
                        "login"
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Crossfade(targetState = isLoading, label = "Loading transition") { loading ->
            if (loading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = modifier
                        .fillMaxSize()
                        .padding()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(bottom = 80.dp), // Espacio para que el FAB no tape nada
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        HomeHeader()
                    }

                    item(span = { GridItemSpan(2) }) {
                        PropertyCountBadge(filteredHouses.size)
                    }

                    item(span = { GridItemSpan(2) }) {
                        SearchSection(searchQuery) { searchQuery = it }
                    }

                    item(span = { GridItemSpan(2) }) {
                        FilterSection(selectedFilter) { selectedFilter = it }
                    }

                    if (filteredHouses.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyState(searchQuery)
                        }
                    } else {
                        items(filteredHouses) { house ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                ModernHouseCard(
                                    house = house,
                                    onClick = { navController.navigate("detail/${house.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.play_store_),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append("Nexus") }
                    append("Homes")
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
        Text(
            text = "Encuentra tu hogar ideal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PropertyCountBadge(count: Int) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "$count propiedades",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text("Disponibles para ti", style = MaterialTheme.typography.bodySmall)
            }
            BadgedBox(badge = { Badge { Text(count.toString()) } }) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SearchSection(query: String, onValueChange: (String) -> Unit) {

    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))

        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            value = query,
            onValueChange = onValueChange,
            label = { Text("Buscar propiedades...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                })
        )
    }
}

@Composable
fun FilterSection(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "Filtrar por tipo",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf("Todas", "Casas", "Cuartos", "Apartamentos")
            types.forEachIndexed { index, type ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(index) },
                    color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = if (selected == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Search,
            null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Text(
            "No hay resultados para '$query'",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}