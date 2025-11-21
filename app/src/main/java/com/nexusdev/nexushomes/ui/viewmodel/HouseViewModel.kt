package com.nexusdev.nexushomes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.nexusdev.nexushomes.model.HouseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Necesitas la dependencia 'kotlinx-coroutines-play-services'

// Usamos un ViewModel para manejar las operaciones de la base de datos de casas
class HomeDataViewModel : ViewModel() {

    // 🔑 Conexión a Firebase
    private val db = FirebaseFirestore.getInstance()
    private val homesCollection = db.collection("homes")

    // --- Estados para la UI (Lectura) ---

    // Lista de casas obtenidas (puede ser una lista de un solo elemento para 'fetchById')
    private val _houses = MutableStateFlow<List<HouseModel>>(emptyList())
    val houses: StateFlow<List<HouseModel>> = _houses.asStateFlow()

    // Estado general de carga y mensajes
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Usamos esta variable para mensajes de error o éxito
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // --- Funciones de Utilidad ---

    fun dismissMessage() {
        _message.value = null
    }

    // --- Funciones de Lectura (Read) ---

    fun fetchHomes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Usamos await() para esperar el resultado de la tarea de Firebase
                val result = homesCollection.get().await()
                val housesList = result.documents.mapNotNull { document ->
                    // Mapeamos el documento a HouseModel
                    val house = document.toObject(HouseModel::class.java)
                    // Asignamos el ID del documento al HouseModel
                    house?.copy(id = document.id)
                }
                _houses.value = housesList
            } catch (e: Exception) {
                _message.value = "Error al obtener las propiedades: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Para obtener los detalles de una casa por su ID de DOCUMENTO
    fun fetchHouseById(documentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val documentSnapshot = homesCollection.document(documentId).get().await()

                val house = documentSnapshot.toObject(HouseModel::class.java)

                if (house != null) {
                    val houseWithId = house.copy(id = documentSnapshot.id)
                    // Actualizamos la lista con el único elemento encontrado
                    _houses.value = listOf(houseWithId)
                } else {
                    _houses.value = emptyList()
                    _message.value = "Propiedad no encontrada."
                }
            } catch (e: Exception) {
                _message.value = "Error al buscar la propiedad: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Función de Escritura (Create) ---

    /**
     * Guarda el objeto HouseModel en Firestore.
     * @param house El objeto HouseModel con las URLs de imagen ya incluidas.
     * @return Boolean indicando si el guardado fue exitoso.
     */
    suspend fun saveHouse(house: HouseModel): Boolean {
        // Esta función ahora es SUSPEND, permitiendo ser llamada desde un ViewModel con corrutinas
        // (idealmente desde el PublishViewModel después de subir las imágenes).

        _isLoading.value = true
        return try {
            val documentReference = homesCollection.add(house).await()
            _message.value = "✅ Publicación creada con éxito! ID: ${documentReference.id}"
            true
        } catch (e: Exception) {
            _message.value = "❌ Error al crear la publicación: ${e.localizedMessage}"
            false
        } finally {
            _isLoading.value = false
        }
    }
}