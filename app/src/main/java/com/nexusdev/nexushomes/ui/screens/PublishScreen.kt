package com.nexusdev.nexushomes.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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

    val viewModel: PublishViewModel = viewModel(
        factory = PublishViewModelFactory(context)
    )

    // Observar el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val details = uiState.propertyDetails // HouseModel
    val selectedImageUris = uiState.selectedImageUris

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addSelectedImageUri(it.toString())
        }
    }

    // para location
    // 1. Define el Launcher para la solicitud de permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido: llama inmediatamente a la función de obtención de ubicación
            viewModel.getCurrentLocation(context)
        } else {
            // Permiso denegado: informa al usuario
            viewModel.showSnackbar("Permiso de ubicación denegado. No se puede obtener la posición actual.")
        }
    }

    // Mostrar Snackbar para mensajes de usuario (errores/éxito)
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage = uiState.userMessage

    LaunchedEffect(userMessage) {
        if (userMessage != null) {
            // Se usa showSnackbar
            snackbarHostState.showSnackbar(userMessage)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    // ⚠️ Corrección: Añadimos un padding top si el Scaffold no lo maneja
                    .padding(top = 16.dp)
                    .height(75.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Publicar una nueva propiedad",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // --- Sección de Subida de Imágenes ---
                    Text(
                        text = "Imágenes (Máx. 3)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ImageUploadSection(
                        selectedImageUris = selectedImageUris,
                        onAddImageClicked = {
                            if (selectedImageUris.size < 3) {
                                imagePickerLauncher.launch("image/*") // Abrir selector de imágenes
                            }
                        },
                        onRemoveImageClicked = viewModel::removeSelectedImageUri
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // --- Fin Sección de Subida de Imágenes ---


                    // Título (Usamos ?.not() de Kotlin para hacer la validación concisa)
                    OutlinedTextField(
                        value = details.title ?: "",
                        onValueChange = { viewModel.updateField(it, "title") },
                        label = { Text("Título de la publicación") },
                        placeholder = { Text("Ej: Hermosa casa de 3 habitaciones...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Descripción
                    OutlinedTextField(
                        value = details.description ?: "",
                        onValueChange = { viewModel.updateField(it, "description") },
                        label = { Text("Descripción") },
                        placeholder = { Text("Describe la propiedad en detalle...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tipo de propiedad
//                    OutlinedTextField(
//                        value = details.type ?: "",
//                        onValueChange = { viewModel.updateField(it, "type") },
//                        label = { Text("Tipo de propiedad") },
//                        placeholder = { Text("Ej: Casa, Apartamento, Villa, etc.") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp),
//                        singleLine = true
//                    )

                    TipoPropiedadSelector(
                        currentType = details.type,
                        onTypeSelected = { viewModel.updateField(it, "type") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Precio
                    OutlinedTextField(
                        value = details.price ?: "",
                        onValueChange = { viewModel.updateField(it, "price") },
                        label = { Text("Precio mensual") },
                        placeholder = { Text("Ej: 1500") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contacto
                    OutlinedTextField(
                        value = details.contact ?: "",
                        onValueChange = { viewModel.updateField(it, "contact") },
                        label = { Text("Información de contacto") },
                        placeholder = { Text("Teléfono o email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dirección
                    OutlinedTextField(
                        value = details.address ?: "",
                        onValueChange = { viewModel.updateField(it, "address") },
                        label = { Text("Dirección completa") },
                        placeholder = { Text("Calle, número, sector...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ubicación (nombre del lugar)
                    OutlinedTextField(
                        value = details.location ?: "",
                        onValueChange = { viewModel.updateField(it, "location") },
                        label = { Text("Nombre de la ubicación") },
                        placeholder = { Text("Ej: Zona Colonial, Piantini, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Coordenadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = details.latitude ?: "",
                            onValueChange = { viewModel.updateField(it, "latitude") },
                            label = { Text("Latitud") },
                            placeholder = { Text("18.4861") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = details.longitude ?: "",
                            onValueChange = { viewModel.updateField(it, "longitude") },
                            label = { Text("Longitud") },
                            placeholder = { Text("-69.9312") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

// --- NUEVA SECCIÓN: Selección de Coordenadas ---
                    Text(
                        text = "Coordenadas GPS",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Lógica: 1. Verifica si el permiso existe
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    // Permiso ya concedido: Ejecuta la lógica directamente
                                    viewModel.getCurrentLocation(context)
                                } else {
                                    // Permiso no concedido: Solicita el permiso
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                                viewModel.getCurrentLocation(context)
                            },
                            // Deshabilita si está cargando o si la ubicación ya fue obtenida/seleccionada
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ubicación Actual")
                        }

                        /*Button(
                            onClick = {
                                // Navegación a la pantalla del mapa
                                // Debes definir la ruta para la pantalla de selección de mapa (ej: "select_map")
                                // navController.navigate("select_map")
                                viewModel.showSnackbar("Función Seleccionar en Mapa (requiere integración con Google Maps o similar)")
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Seleccionar en Mapa")
                        }*/
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón de publicación
                    Button(
                        onClick = { viewModel.publishProperty() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
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

// Composable para la selección y visualización de imágenes (Sin cambios)
@Composable
fun ImageUploadSection(
    selectedImageUris: List<String>,
    onAddImageClicked: () -> Unit,
    onRemoveImageClicked: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ítem para el botón de añadir imagen
        item {
            AddImageButton(
                onClick = onAddImageClicked,
                isEnabled = selectedImageUris.size < 3
            )
        }

        // Ítems para las imágenes seleccionadas
        items(selectedImageUris) { uriString ->
            SelectedImagePreview(
                uriString = uriString,
                onRemoveClicked = { onRemoveImageClicked(uriString) }
            )
        }
    }
}

// Botón para añadir una nueva imagen (Sin cambios)
@Composable
fun AddImageButton(
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick, enabled = isEnabled)
            .border(
                width = 2.dp,
                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Añadir imagen",
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// Previsualización de la imagen seleccionada (Sin cambios)
@Composable
fun SelectedImagePreview(
    uriString: String,
    onRemoveClicked: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        // Usar Coil para cargar la imagen localmente desde la URI
        val painter = rememberAsyncImagePainter(Uri.parse(uriString))
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        // Botón de eliminar (X)
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clickable(onClick = onRemoveClicked)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar imagen",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoPropiedadSelector(
    currentType: String?,
    onTypeSelected: (String) -> Unit
) {
    // 1. Lista de opciones disponibles
    val propertyTypes =
        listOf("Casa", "Apartamento", "Habitación", "Lote", "Local Comercial", "Oficina")

    // 2. Estado para controlar si el menú desplegable está expandido
    var expanded by remember { mutableStateOf(false) }

    // 3. Texto visible: usa el valor seleccionado o un texto predeterminado
    val selectedText = currentType ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        // El OutlinedTextField sirve como el contenedor visible
        OutlinedTextField(
            // El modificador 'menuAnchor' es necesario para Compose 1.5+
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true, // No permite escribir, solo seleccionar
            value = selectedText,
            onValueChange = {}, // No se usa ya que es de solo lectura
            label = { Text("Tipo de propiedad") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            placeholder = { Text("Selecciona el tipo...") },
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        // El menú desplegable que aparece al hacer clic
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            propertyTypes.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onTypeSelected(selectionOption) // Notifica al ViewModel la selección
                        expanded = false // Cierra el menú después de seleccionar
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}