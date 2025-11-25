package com.nexusdev.nexushomes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.nexusdev.nexushomes.model.HouseModel
import com.nexusdev.nexushomes.model.UiState
import com.nexusdev.nexushomes.utils.ImageKitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PublishViewModel(private val repository: ImageKitRepository) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val homesCollection = db.collection("homes")

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun dismissMessage() { _uiState.value = _uiState.value.copy(userMessage = null) }

    fun addSelectedImageUri(uri: String) {
        val current = _uiState.value.selectedImageUris.toMutableList()
        if (current.size < 3) {
            current.add(uri)
            _uiState.value = _uiState.value.copy(selectedImageUris = current)
        }
    }

    fun removeSelectedImageUri(uri: String) {
        val current = _uiState.value.selectedImageUris.toMutableList()
        current.remove(uri)
        _uiState.value = _uiState.value.copy(selectedImageUris = current)
    }

    fun updateField(value: String, field: String) {
        val details = _uiState.value.propertyDetails
        val updated = when (field) {
            "title" -> details.copy(title = value)
            "description" -> details.copy(description = value)
            "type" -> details.copy(type = value)
            "price" -> details.copy(price = value)
            "contact" -> details.copy(contact = value)
            "address" -> details.copy(address = value)
            "location" -> details.copy(location = value)
            "latitude" -> details.copy(latitude = value)
            "longitude" -> details.copy(longitude = value)
            else -> details
        }
        _uiState.value = _uiState.value.copy(propertyDetails = updated)
    }

    fun publishProperty() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 1️⃣ Obtener auth
                val auth = repository.getAuth()

                // 2️⃣ Subir imágenes
                val urls = _uiState.value.selectedImageUris.map { uri ->
                    repository.uploadImage(uri, auth)
                }

                // 3️⃣ Guardar en Firestore
                val house = _uiState.value.propertyDetails.copy(image = ArrayList(urls))
                homesCollection.add(house).addOnSuccessListener {
                    _uiState.value = _uiState.value.copy(userMessage = "✅ Publicación creada con éxito!")
                }.addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(userMessage = "❌ Error al crear la publicación: ${e.localizedMessage}")
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(userMessage = "❌ Error: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
