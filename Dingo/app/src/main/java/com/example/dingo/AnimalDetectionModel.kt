package com.example.dingo

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.json.JSONObject


class AnimalDetectionModel(context : Context) {
    private val context = context
    private val modelInputImageDim = 320
    private val isQuantized = false


    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        ) // Change format and quality as needed
        return outputStream.toByteArray()
    }

    public fun run (input : Bitmap, callback: (String) -> Unit, saveAsDefault: Boolean, saveStorage: Boolean,
                    savePicture: (entryName: String, image: Bitmap, saveAsDefault: Boolean, saveImage: Boolean, context: Context) -> Unit,
                    addEntry: (entryName: String) -> Unit) : IntArray {
        val mutableCopy: Bitmap = input.copy(input.config, true)
        val refitImage = Bitmap.createScaledBitmap(input, modelInputImageDim, modelInputImageDim, false)
         val imageBytes = convertBitmapToByteArray(refitImage);

        GlobalScope.launch {
            try {
                val response = sendPostRequest(imageBytes)
                // Handle the response data
                val responseData = response.body?.string()
                println(responseData)
                // add to entry to dingodex
                val name = JSONObject(responseData)
                val processedImageValue = name.getString("processed_image")
                if( processedImageValue != "not found") {
                    addEntry(processedImageValue)
                    savePicture(processedImageValue, mutableCopy, saveAsDefault, saveStorage, context)
                }
                callback(processedImageValue)

                // Use responseData as needed
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return intArrayOf(1,2)
    }

    private fun sendPostRequest(imageBytes: ByteArray): okhttp3.Response {

        val client = OkHttpClient.Builder()
            .connectTimeout(80, TimeUnit.SECONDS) // Set connection timeout to 40 seconds
            .readTimeout(80, TimeUnit.SECONDS)    // Set read timeout to 40 seconds
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", imageBytes.toRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("http://192.168.2.166:8000/api/process_image/")
            .post(requestBody)
            .build()

        return client.newCall(request).execute()
    }

}