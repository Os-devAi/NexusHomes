package com.nexusdev.nexushomes.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.nexusdev.nexushomes.ui.viewmodel.PublishViewModel
import com.nexusdev.nexushomes.ui.viewmodel.PublishViewModelFactory

@Composable
fun PublishScreen(
    navController: NavHostController,
    modifier: Modifier
) {
    val context = LocalContext.current
    val viewModel: PublishViewModel = viewModel(factory = PublishViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val details = uiState.propertyDetails
    val selectedImageUris = uiState.selectedImageUris

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.addSelectedImageUri(it.toString()) } }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.getCurrentLocation(context)
        else viewModel.showSnackbar("Permiso de ubicación denegado.")
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage = uiState.userMessage

    LaunchedEffect(userMessage) {
        if (userMessage != null) {
            snackbarHostState.showSnackbar(userMessage)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(75.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Publicar una nueva propiedad",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Imágenes (Mín. 1 - Máx. 3)",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selectedImageUris.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ImageUploadSection(
                        selectedImageUris = selectedImageUris,
                        onAddImageClicked = {
                            if (selectedImageUris.size < 3) imagePickerLauncher.launch(
                                "image/*"
                            )
                        },
                        onRemoveImageClicked = viewModel::removeSelectedImageUri
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = details.title ?: "",
                        onValueChange = { viewModel.updateField(it, "title") },
                        label = { Text("Título de la publicación") },
                        placeholder = { Text("Ej: Hermosa casa de 3 habitaciones...") },
                        isError = details.title.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = details.description ?: "",
                        onValueChange = { viewModel.updateField(it, "description") },
                        label = { Text("Descripción") },
                        placeholder = { Text("Describe la propiedad en detalle...") },
                        isError = details.description.isNullOrBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TipoPropiedadSelector(
                        currentType = details.type,
                        onTypeSelected = { viewModel.updateField(it, "type") }
                    )
                    if (details.type.isNullOrBlank()) {
                        Text(
                            "Seleccione un tipo",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = details.price ?: "",
                        onValueChange = { viewModel.updateField(it, "price") },
                        label = { Text("Precio mensual") },
                        placeholder = { Text("Ej: 1500") },
                        isError = details.price.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = details.contact ?: "",
                        onValueChange = { viewModel.updateField(it, "contact") },
                        label = { Text("Información de contacto") },
                        placeholder = { Text("Teléfono o email") },
                        isError = details.contact.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = details.address ?: "",
                        onValueChange = { viewModel.updateField(it, "address") },
                        label = { Text("Dirección completa") },
                        placeholder = { Text("Calle, número, sector...") },
                        isError = details.address.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = details.location ?: "",
                        onValueChange = { viewModel.updateField(it, "location") },
                        label = { Text("Nombre de la ubicación (Sector)") },
                        placeholder = { Text("Ej: Zona Colonial, Piantini, etc.") },
                        isError = details.location.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = details.latitude ?: "",
                            onValueChange = { viewModel.updateField(it, "latitude") },
                            label = { Text("Latitud") },
                            placeholder = { Text("18.4861") },
                            isError = details.latitude.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = details.longitude ?: "",
                            onValueChange = { viewModel.updateField(it, "longitude") },
                            label = { Text("Longitud") },
                            placeholder = { Text("-69.9312") },
                            isError = details.longitude.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Coordenadas GPS",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa las coordenadas exactas de la propiedad de forma manual o usa la ubicación actual",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                viewModel.getCurrentLocation(context)
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        Text("Obtener Ubicación Actual")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.publishProperty() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publicar propiedad")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}

@Composable
fun ImageUploadSection(
    selectedImageUris: List<String>,
    onAddImageClicked: () -> Unit,
    onRemoveImageClicked: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { AddImageButton(onClick = onAddImageClicked, isEnabled = selectedImageUris.size < 3) }
        items(selectedImageUris) { uri ->
            SelectedImagePreview(uriString = uri, onRemoveClicked = { onRemoveImageClicked(uri) })
        }
    }
}

@Composable
fun AddImageButton(onClick: () -> Unit, isEnabled: Boolean) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .border(
                2.dp,
                if (isEnabled) MaterialTheme.colorScheme.primary else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.AddCircle,
                null,
                modifier = Modifier.size(40.dp),
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.LightGray
            )
        }
    }
}

@Composable
fun SelectedImagePreview(uriString: String, onRemoveClicked: () -> Unit) {
    Box(modifier = Modifier.size(100.dp)) {
        Image(
            painter = rememberAsyncImagePainter(Uri.parse(uriString)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
        Surface(
            color = Color.Black.copy(0.6f), shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clickable { onRemoveClicked() }
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoPropiedadSelector(currentType: String?, onTypeSelected: (String) -> Unit) {
    val propertyTypes =
        listOf("Casa", "Apartamento", "Habitación", "Lote", "Local Comercial", "Oficina")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = currentType ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de propiedad") },
            placeholder = { Text("Selecciona el tipo...") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            isError = currentType.isNullOrBlank()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            propertyTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = { onTypeSelected(type); expanded = false })
            }
        }
    }
}