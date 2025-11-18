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

                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}