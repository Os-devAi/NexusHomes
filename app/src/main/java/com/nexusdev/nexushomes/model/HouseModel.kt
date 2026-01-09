package com.nexusdev.nexushomes.model

data class HouseModel(
    var id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val contact: String? = null,
    val type: String? = null,
    val price: String? = null,
    val address: String? = null,
    val location: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val image: ArrayList<String>? = null,
    val status: String? = null,
    val userId: String? = null
)

data class UiState(
    // Usamos HouseModel como el modelo central del formulario
    val propertyDetails: HouseModel = HouseModel(),

    // URIs locales de las imágenes seleccionadas antes de la subida
    val selectedImageUris: List<String> = emptyList(),

    val isLoading: Boolean = false,
    val userMessage: String? = null
)