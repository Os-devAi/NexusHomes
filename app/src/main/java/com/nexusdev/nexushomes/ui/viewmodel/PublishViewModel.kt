package com.nexusdev.nexushomes.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nexusdev.nexushomes.model.HouseModel
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

    // ----------------------------
    // 🔵 Seleccionar imágenes
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
    // 🔵 Actualizar campos del formulario
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
    // 🔵 Subir 1 imagen comprimida
    // ----------------------------
    private suspend fun uploadCompressedImage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
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

    // ----------------------------
    // 🔵 PUBLICAR PROPIEDAD
    // ----------------------------
    fun publishProperty() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 1️⃣ Subir imágenes
                val uploadedUrls = uploadAllImages()

                if (uploadedUrls.isEmpty()) {
                    _uiState.value =
                        _uiState.value.copy(userMessage = "❌ No se pudieron subir las imágenes.")
                    return@launch
                }

                // 2️⃣ Crear modelo final
                val finalHouse = _uiState.value.propertyDetails.copy(
                    image = ArrayList(uploadedUrls)
                )

                // 3️⃣ Guardar en Firestore
                homesCollection.add(finalHouse).await()

                _uiState.value =
                    _uiState.value.copy(userMessage = "✅ Publicación creada con éxito!")

                // Limpiar formulario
                _uiState.value = UiState()

            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(userMessage = "❌ Error: ${e.localizedMessage}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
