package com.nexusdev.nexushomes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nexusdev.nexushomes.model.HouseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    fun dismissMessage() {
        _message.value = null
    }

    // -------------------------------
    // 🔵 OBTENER CASAS
    // -------------------------------
    fun fetchHomes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = homesCollection
                    .whereEqualTo("status", "Activo")
                    .get().await()
                val housesList = result.documents.mapNotNull { doc ->
                    doc.toObject(HouseModel::class.java)?.copy(id = doc.id)
                }
                _houses.value = housesList
            } catch (e: Exception) {
                _message.value = "Error al obtener propiedades: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyPublish(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = homesCollection
                    .whereEqualTo("userId", userId)
                    .get().await()
                val housesList = result.documents.mapNotNull { doc ->
                    doc.toObject(HouseModel::class.java)?.copy(id = doc.id)
                }
                _houses.value = housesList
            } catch (e: Exception) {
                _message.value = "Error al buscar la propiedad: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
