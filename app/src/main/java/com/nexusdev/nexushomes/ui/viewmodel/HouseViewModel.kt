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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class HomeDataViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val homesCollection = db.collection("homes")
    private val storage = FirebaseStorage.getInstance().reference

    private val _houses = MutableStateFlow<List<HouseModel>>(emptyList())
    val houses: StateFlow<List<HouseModel>> = _houses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun dismissMessage() { _message.value = null }

    // -------------------------------
    // 🔵 OBTENER CASAS
    // -------------------------------
    fun fetchHomes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = homesCollection.get().await()
                val housesList = result.documents.mapNotNull { doc ->
                    doc.toObject(HouseModel::class.java)?.copy(id = doc.id)
                }
                _houses.value = housesList
            } catch (e: Exception) {
                _message.value = "Error al obtener propiedades: ${e.localizedMessage}"
            } finally { _isLoading.value = false }
        }
    }

    fun fetchHouseById(documentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snap = homesCollection.document(documentId).get().await()
                val house = snap.toObject(HouseModel::class.java)

                _houses.value = house?.let { listOf(it.copy(id = snap.id)) } ?: emptyList()

            } catch (e: Exception) {
                _message.value = "Error al buscar la propiedad: ${e.localizedMessage}"
            } finally { _isLoading.value = false }
        }
    }

    // -------------------------------
    // 🔵 SUBIR IMÁGENES COMPRESAS A STORAGE
    // -------------------------------
    private suspend fun uploadCompressedImage(
        context: Context,
        imageUri: Uri
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Convertir a bitmap
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // Comprimir imagen al 70%
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val compressedBytes = baos.toByteArray()

            // Generar nombre
            val fileName = "houses/${UUID.randomUUID()}.jpg"
            val ref = storage.child(fileName)

            // Subir
            ref.putBytes(compressedBytes).await()

            // Obtener URL pública
            return@withContext ref.downloadUrl.await().toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------------
    // 🔵 SUBIR LAS 3 IMÁGENES
    // -------------------------------
    suspend fun uploadHouseImages(
        context: Context,
        uris: List<String>
    ): List<String> {
        val uploadedUrls = mutableListOf<String>()

        for (uriStr in uris) {
            val url = uploadCompressedImage(context, Uri.parse(uriStr))
            if (url != null) uploadedUrls.add(url)
        }
        return uploadedUrls
    }

    // -------------------------------
    // 🔵 GUARDAR PUBLICACIÓN
    // -------------------------------
    suspend fun saveHouseWithImages(
        context: Context,
        house: HouseModel,
        imageUris: List<String>
    ): Boolean {
        _isLoading.value = true

        return try {
            // 1️⃣ Subir fotos
            val uploadedUrls = uploadHouseImages(context, imageUris)

            if (uploadedUrls.isEmpty()) {
                _message.value = "❌ No se pudieron subir las imágenes."
                return false
            }

            // 2️⃣ Agregar URLs al modelo
            val finalHouse = house.copy(image = ArrayList(uploadedUrls))

            // 3️⃣ Guardar en Firestore
            homesCollection.add(finalHouse).await()

            _message.value = "✅ Publicación creada con éxito"
            true

        } catch (e: Exception) {
            _message.value = "❌ Error al guardar: ${e.localizedMessage}"
            false
        } finally {
            _isLoading.value = false
        }
    }
}
