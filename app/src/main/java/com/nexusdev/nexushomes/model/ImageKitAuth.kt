package com.nexusdev.nexushomes.model

data class ImageKitAuth(
    val token: String,
    val signature: String,
    val expire: Long,
    val publicKey: String,
    val urlEndpoint: String
)
