package com.nexusdev.nexushomes.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ImageKitRepository(private val context: Context) {

    private val apiService: ImageKitApiService

    init {
        // Interceptor para ver las peticiones en Logcat (útil para debug)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Cliente OkHttpClient configurado
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Cliente Retrofit con la URL base y el Scalars Converter
        val retrofit = Retrofit.Builder()
            .baseUrl("https://upload.imagekit.io/")
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
            .build()

        apiService = retrofit.create(ImageKitApiService::class.java)
    }

    /**
     * Sube un archivo a ImageKit.io utilizando Retrofit y multipart/form-data.
     * @param imageUri La URI local del archivo de imagen seleccionado (ej: content://...).
     * @return Result<String> que contiene la URL pública de la imagen subida.
     */
    suspend fun uploadImage(imageUri: String): Result<String> {
        val uri = Uri.parse(imageUri)
        val fileName = "propiedad_${System.currentTimeMillis()}.jpg"

        return try {
            // 1. Leer el archivo como un ByteArray
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: throw IllegalStateException("No se pudo leer el archivo desde la URI.")

            // 2. Crear el RequestBody a partir del ByteArray
            val requestFile = bytes.toRequestBody(
                contentType = "image/*".toMediaTypeOrNull(), // Intentamos adivinar el tipo MIME o usamos un genérico
                offset = 0,
                byteCount = bytes.size
            )

            // 3. Crear el MultipartBody.Part para el archivo
            val filePart = MultipartBody.Part.createFormData(
                name = "file", // El nombre del campo que espera ImageKit
                filename = fileName,
                body = requestFile
            )

            // 4. Crear los RequestBody para los campos de texto
            val fileNameBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
            val publicKeyBody =
                ImageKitManager.PUBLIC_KEY.toRequestBody("text/plain".toMediaTypeOrNull())
            val privateKeyBody =
                ImageKitManager.PRIVATE_KEY.toRequestBody("text/plain".toMediaTypeOrNull())
            val uniqueFileNameBody = "true".toRequestBody("text/plain".toMediaTypeOrNull())

            // 5. Llamada a la API de ImageKit.io
            val responseString = apiService.uploadFile(
                file = filePart,
                fileName = fileNameBody,
                publicKey = publicKeyBody,
                privateKey = privateKeyBody,
                useUniqueFileName = uniqueFileNameBody
            )

            // 6. Parsear la respuesta String a un objeto JSON para extraer la URL
            val jsonObject = JSONObject(responseString)
            val url = jsonObject.optString("url")

            if (url.isNullOrEmpty()) {
                // Si ImageKit responde con éxito, pero la URL no está
                Result.failure(Exception("Subida exitosa, pero no se encontró la URL en la respuesta."))
            } else {
                Result.success(url)
            }

        } catch (e: Exception) {
            // Manejar errores de red, IO o fallos en la respuesta (ej. 401, 500)
            Result.failure(
                Exception(
                    "Error de subida o red con Retrofit: ${e.localizedMessage}",
                    e
                )
            )
        }
    }
}