package com.nexusdev.nexushomes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusdev.nexushomes.utils.ImageKitRepository
import com.nexusdev.nexushomes.model.HouseModel
import com.nexusdev.nexushomes.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.ArrayList

// Se ha eliminado la importación innecesaria: import androidx.compose.ui.platform.LocalContext

class PublishViewModel(
    // ⚠️ Importante: Si ImageKitRepository requiere Context (como en la implementación de Retrofit),
    // debes ASEGURARTE de que SIEMPRE se inyecte aquí a través del ViewModelFactory.
    // El valor por defecto (ImageKitRepository()) solo debería usarse si el repositorio NO requiere Context
    // y para que el código compile, pero el Factory debe encargarse de la inyección.
    private val imageKitRepository: ImageKitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // --- Funciones para actualizar el estado del formulario ---

    fun updateDetails(block: (HouseModel) -> HouseModel) {
        _uiState.update {
            it.copy(propertyDetails = block(it.propertyDetails))
        }
    }

    // Función de ayuda para actualizar campos específicos del HouseModel
    fun updateField(newValue: String, field: String) {
        updateDetails { details ->
            when (field) {
                "title" -> details.copy(title = newValue)
                "description" -> details.copy(description = newValue)
                "contact" -> details.copy(contact = newValue)
                "type" -> details.copy(type = newValue)
                "price" -> details.copy(price = newValue)
                "address" -> details.copy(address = newValue)
                "location" -> details.copy(location = newValue)
                "latitude" -> details.copy(latitude = newValue)
                "longitude" -> details.copy(longitude = newValue)
                else -> details
            }
        }
    }

    // --- Funciones para manejar la selección de imágenes (URIs locales) ---

    fun addSelectedImageUri(uri: String) {
        _uiState.update { currentState ->
            // Se mantiene el límite de 3 imágenes
            if (currentState.selectedImageUris.size < 3 && uri !in currentState.selectedImageUris) {
                currentState.copy(
                    selectedImageUris = currentState.selectedImageUris + uri
                )
            } else {
                currentState
            }
        }
    }

    fun removeSelectedImageUri(uri: String) {
        _uiState.update {
            it.copy(selectedImageUris = it.selectedImageUris - uri)
        }
    }

    // --- Lógica principal de publicación ---

    fun publishProperty() {
        val details = _uiState.value.propertyDetails
        val uris = _uiState.value.selectedImageUris

        if (!isValid(details)) {
            _uiState.update { it.copy(userMessage = "Por favor, completa los campos requeridos (Título, Precio, Dirección).") }
            return
        }

        if (uris.isEmpty()) {
            _uiState.update { it.copy(userMessage = "Por favor, selecciona al menos una imagen.") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                userMessage = "Iniciando subida de imágenes..."
            )
        }

        viewModelScope.launch {
            val uploadedUrls = mutableListOf<String>()
            var allSuccessful = true

            // 1. Subir cada imagen a ImageKit.io
            for (uri in uris) {
                _uiState.update { it.copy(userMessage = "Subiendo imagen ${uploadedUrls.size + 1} de ${uris.size}...") }
                val result = imageKitRepository.uploadImage(uri)

                if (result.isSuccess) {
                    uploadedUrls.add(result.getOrThrow())
                } else {
                    allSuccessful = false
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Fallo al subir la imagen: ${result.exceptionOrNull()?.localizedMessage}"
                        )
                    }
                    break // Detener si falla una subida
                }
            }

            // 2. Si todas las imágenes se subieron, guardar los detalles de la propiedad
            if (allSuccessful) {
                // Creamos el HouseModel final con las URLs de ImageKit
                val finalDetails = details.copy(
                    image = ArrayList(uploadedUrls)
                )

                // Aquí iría la lógica para guardar 'finalDetails' en Firebase Firestore
                // o tu Backend.

                // Simulación de éxito y limpieza
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "✅ Propiedad publicada con éxito! URLs: ${finalDetails.image}",
                        propertyDetails = HouseModel(), // Limpiar formulario
                        selectedImageUris = emptyList() // Limpiar imágenes seleccionadas
                    )
                }
            }
        }
    }

    // Lógica de validación
    private fun isValid(details: HouseModel): Boolean {
        return details.title.isNullOrBlank().not() &&
                details.price.isNullOrBlank().not() &&
                details.address.isNullOrBlank().not()
    }

    fun dismissMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}