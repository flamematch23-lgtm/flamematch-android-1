package com.flamematch.app.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CloudinaryRepository {
    
    companion object {
        private const val CLOUD_NAME = "dnxqpp2en"
        private const val UPLOAD_PRESET = "flamematch_photos"
        private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun uploadImage(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Copy URI to temp file
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            tempFile.delete()
            
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val secureUrl = jsonResponse.getString("secure_url")
                Result.success(secureUrl)
            } else {
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadVoice(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("resource_type", "video") // Cloudinary uses video for audio
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("audio/m4a".toMediaType())
                )
                .build()
            
            val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/video/upload"
            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            tempFile.delete()
            
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                val secureUrl = jsonResponse.getString("secure_url")
                Result.success(secureUrl)
            } else {
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
