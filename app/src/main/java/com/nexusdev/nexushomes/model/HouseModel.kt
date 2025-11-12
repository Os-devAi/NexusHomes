package com.nexusdev.nexushomes.model

data class HouseModel(
    var id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val price: String? = null,
    val address: String? = null,
    val location: String? = null,
    val image: ArrayList<String>? = null
)
