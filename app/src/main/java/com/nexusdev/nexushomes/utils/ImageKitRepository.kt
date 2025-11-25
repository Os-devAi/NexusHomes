package com.nexusdev.nexushomes.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.nexusdev.nexushomes.model.ImageKitAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ImageKitRepository(private val context: Context) {

    private val client = OkHttpClient()
    private val backendUrl = "https://backend-imagekit.onrender.com/auth"

    // 1️⃣ Obtener auth desde tu backend
    suspend fun getAuth(): ImageKitAuth = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(backendUrl).get().build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Error al obtener auth: ${response.code}")

        val json = JSONObject(response.body?.string() ?: "")
        return@withContext ImageKitAuth(
            token = json.getString("token"),
            signature = json.getString("signature"),
            expire = json.getLong("expire"),
            publicKey = json.getString("publicKey"),
            urlEndpoint = json.getString("urlEndpoint")
        )
    }

    // 2️⃣ Convertir URI a File
    private fun uriToFile(uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir la imagen")
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val output = FileOutputStream(file)
        input.copyTo(output)
        input.close()
        output.close()
        return file
    }

    // 3️⃣ Subir imagen usando auth
    suspend fun uploadImage(uriString: String, auth: ImageKitAuth): String = withContext(Dispatchers.IO) {
        val file = uriToFile(uriString.toUri())

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .addFormDataPart("fileName", file.name)
            .addFormDataPart("publicKey", auth.publicKey)
            .addFormDataPart("signature", auth.signature)
            .addFormDataPart("expire", auth.expire.toString())
            .addFormDataPart("token", auth.token)
            .build()

        val request = Request.Builder()
            .url("https://upload.imagekit.io/api/v1/files/upload")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Error al subir imagen: ${response.code}")

        val json = JSONObject(response.body?.string() ?: "")
        return@withContext json.getString("url")
    }
}
