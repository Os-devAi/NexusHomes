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

    // funcioens para modificar o eliminar la publicacion
    fun updateHouse(house: HouseModel) {
        val houseId = house.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Actualiza el documento en Firestore
                homesCollection.document(houseId).set(house).await()
                _message.value = "Propiedad actualizada con éxito"

                // Opcional: Refrescar la lista localmente
                _houses.value = _houses.value.map { if (it.id == houseId) house else it }
            } catch (e: Exception) {
                _message.value = "Error al actualizar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHouse(houseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Elimina de Firestore
                homesCollection.document(houseId).delete().await()

                // Actualiza el estado local para reflejar la eliminación de inmediato
                _houses.value = _houses.value.filter { it.id != houseId }

                _message.value = "Propiedad eliminada correctamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
