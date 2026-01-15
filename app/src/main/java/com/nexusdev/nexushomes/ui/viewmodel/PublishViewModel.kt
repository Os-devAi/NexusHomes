package com.nexusdev.nexushomes.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nexusdev.nexushomes.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlinx.coroutines.flow.update

class PublishViewModel(
    private val context: Context
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val homesCollection = db.collection("homes")
    private val storage = FirebaseStorage.getInstance().reference

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    // funcion para obtener la localizacion de google maps
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        // 1. Verificar Permisos
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No tienes permisos.
            // Aquí debes solicitar los permisos usando un ActivityResultLauncher en tu Activity o Composable principal.
            // Para fines de la UI, puedes mostrar un mensaje:
            showSnackbar("Permiso de Ubicación requerido para obtener la posición actual.")
            return
        }

        // Ejecutar en coroutine para no bloquear UI
        viewModelScope.launch {
            try {
                val fused = LocationServices.getFusedLocationProviderClient(context)

                // 1) Intentar lastLocation rápido
                val lastLocation = try {
                    fused.lastLocation.await()
                } catch (e: Exception) {
                    null
                }

                val location = lastLocation ?: run {
                    // 2) Si lastLocation es null, pedir una ubicación actual de alta precisión (una sola lectura)
                    val cts = CancellationTokenSource()
                    try {
                        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                    } finally {
                        // asegurarnos de cancelar el token si la coroutine termina
                        cts.cancel()
                    }
                }

                if (location != null) {
                    // Actualizar campos con lat / lon reales
                    updateField(location.latitude.toString(), "latitude")
                    updateField(location.longitude.toString(), "longitude")
                    showSnackbar("Ubicación obtenida correctamente.")
                } else {
                    // Puede que el GPS esté apagado o no haya señal; informar al usuario
                    showSnackbar("No se pudo obtener la ubicación. Verifica que el GPS esté activado.")
                }
            } catch (e: Exception) {
                // Manejo de errores (por ejemplo Settings no correctos, timeouts, etc.)
                showSnackbar("Error al obtener ubicación: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(userMessage = message) }
    }

    // ----------------------------
    // seleccionar imágenes
    // ----------------------------
    fun addSelectedImageUri(uri: String) {
        val list = _uiState.value.selectedImageUris.toMutableList()
        if (list.size < 3) {
            list.add(uri)
            _uiState.value = _uiState.value.copy(selectedImageUris = list)
        }
    }

    fun removeSelectedImageUri(uri: String) {
        val list = _uiState.value.selectedImageUris.toMutableList()
        list.remove(uri)
        _uiState.value = _uiState.value.copy(selectedImageUris = list)
    }

    // ----------------------------
    // actualizar campos del formulario
    // ----------------------------
    fun updateField(value: String, field: String) {
        val d = _uiState.value.propertyDetails
        val updated = when (field) {
            "title" -> d.copy(title = value)
            "description" -> d.copy(description = value)
            "type" -> d.copy(type = value)
            "price" -> d.copy(price = value)
            "contact" -> d.copy(contact = value)
            "address" -> d.copy(address = value)
            "location" -> d.copy(location = value)
            "latitude" -> d.copy(latitude = value)
            "longitude" -> d.copy(longitude = value)
            else -> d
        }
        _uiState.value = _uiState.value.copy(propertyDetails = updated)
    }

    // ----------------------------
    // subir 1 imagen comprimida
    // ----------------------------
    private suspend fun uploadCompressedImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val compressed = baos.toByteArray()

            val filename = "houses/${UUID.randomUUID()}.jpg"
            val ref = storage.child(filename)

            ref.putBytes(compressed).await()

            ref.downloadUrl.await().toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ----------------------------
    // 🔵 Subir varias imágenes
    // ----------------------------
    private suspend fun uploadAllImages(): List<String> {
        val urls = mutableListOf<String>()

        for (uri in _uiState.value.selectedImageUris) {
            uploadCompressedImage(Uri.parse(uri))?.let { urls.add(it) }
        }

        return urls
    }


    // validar campos no vacios
    private fun validateFields(): String? {
        val d = _uiState.value.propertyDetails
        val uris = _uiState.value.selectedImageUris

        return when {
            uris.isEmpty() -> "Selecciona al menos una imagen para tu propiedad."
            d.title.isNullOrBlank() -> "El título de la publicación es obligatorio."
            d.description.isNullOrBlank() -> "Por favor, añade una descripción detallada."
            d.type.isNullOrBlank() -> "Debes seleccionar un tipo de propiedad."
            d.price.isNullOrBlank() -> "El precio es obligatorio."
            d.contact.isNullOrBlank() -> "Añade un número o email de contacto."
            d.address.isNullOrBlank() -> "La dirección física es necesaria."
            d.location.isNullOrBlank() -> "Indica el nombre del sector o zona."
            d.latitude.isNullOrBlank() || d.longitude.isNullOrBlank() -> "Las coordenadas GPS son obligatorias."
            else -> null
        }
    }


    // funcion para publicar la pantalla
    fun publishProperty() {
        viewModelScope.launch {
            // 1. Ejecutar validación antes de empezar cualquier proceso
            val validationError = validateFields()
            if (validationError != null) {
                showSnackbar("⚠️ $validationError")
                return@launch
            }

            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 2. Subir imágenes (ahora solo ocurre si los campos están llenos)
                val uploadedUrls = uploadAllImages()

                if (uploadedUrls.isEmpty()) {
                    showSnackbar("❌ No se pudieron subir las imágenes.")
                    return@launch
                }

                // 3. Crear modelo final
                val finalHouse = _uiState.value.propertyDetails.copy(
                    image = ArrayList(uploadedUrls),
                    status = "Activo",
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                )

                // 4. Guardar en Firestore
                homesCollection.add(finalHouse).await()

                _uiState.value = _uiState.value.copy(
                    userMessage = "✅ Publicación creada con éxito!",
                    // Reiniciamos todo el estado para limpiar el formulario
                    propertyDetails = com.nexusdev.nexushomes.model.HouseModel(),
                    selectedImageUris = emptyList()
                )

            } catch (e: Exception) {
                showSnackbar("❌ Error: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
