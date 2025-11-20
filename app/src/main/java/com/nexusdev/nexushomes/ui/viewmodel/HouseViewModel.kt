package com.nexusdev.nexushomes.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.nexusdev.nexushomes.model.HouseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.emptyList

class HouseViewModel : ViewModel() {

    // utils val variables
    private val db = FirebaseFirestore.getInstance()

    private val _houses = MutableStateFlow<List<HouseModel?>>(emptyList())
    val houses: StateFlow<List<HouseModel?>> = _houses.asStateFlow()

    val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()


    fun fetchHomes() {
        try {
            _isLoading.value = true
            db.collection("homes")
                .get()
                .addOnSuccessListener { result ->
                    val housesList = mutableListOf<HouseModel>()
                    for (document in result) {
                        val house = document.toObject(HouseModel::class.java)
                        house.id = document.id
                        housesList.add(house)
                    }
                    _houses.value = housesList
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _message.value = exception.message.toString()
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // para obtener los detalles llamar por id
    fun fetchById(id: String) {
        try {
            _isLoading.value = true
            db.collection("homes")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener { result ->
                    val housesList = mutableListOf<HouseModel>()
                    for (document in result) {
                        val house = document.toObject(HouseModel::class.java)
                        house.id = document.id
                        housesList.add(house)
                    }
                    _houses.value = housesList
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _message.value = exception.message.toString()
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // crear una nueva publicacieón
    fun createHouse(house: HouseModel) {
        try {
            _isLoading.value = true
            db.collection("homes")
                .add(house)
                .addOnSuccessListener { documentReference ->
                    _message.value = "Publicación creada con ID: ${documentReference.id}"
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _message.value = exception.message.toString()
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}