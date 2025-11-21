package com.nexusdev.nexushomes.utils

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageKitApiService {

    // El endpoint de ImageKit (https://upload.imagekit.io/api/v1/files/upload)
    // Se define la base URL como "https://upload.imagekit.io/" en el cliente Retrofit.

    @Multipart
    @POST("api/v1/files/upload")
    suspend fun uploadFile(
        // Parámetros de tipo Part para el formulario multipart
        @Part file: MultipartBody.Part,
        @Part("fileName") fileName: RequestBody,
        @Part("publicKey") publicKey: RequestBody,
        @Part("privateKey") privateKey: RequestBody,
        @Part("useUniqueFileName") useUniqueFileName: RequestBody
        // ... otros parámetros como "folder"
    ): String // Retrofit devuelve la respuesta como String (gracias al Scalars Converter)
}