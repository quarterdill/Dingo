package com.example.dingo

import android.content.Context
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.channels.FileChannel
import androidx.lifecycle.lifecycleScope
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


class AnimalDetectionModel(context : Context) {
    private val context = context
    private val modelInputImageDim = 320
    private val isQuantized = false
//    private val maxDetections = 5
//    private val confidenceScoresTensorShape = intArrayOf( 1 , maxDetections )
    private val classesTensorShape = intArrayOf( 1 , 5 )
//    private val inputImageProcessorQuantized = ImageProcessor.Builder()
//        .add( ResizeOp( modelInputImageDim , modelInputImageDim , ResizeOp.ResizeMethod.BILINEAR ) )
//        .add( CastOp( DataType.FLOAT32 ) )
//        .build()
    private val numThreads = 4
    private val assetManager = context.assets
//    private var animalClassifier: ImageClassifier? = null

    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        ) // Change format and quality as needed
        return outputStream.toByteArray()
    }
//    private fun convertBitmapToByteArray( input :) {
//        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
//            .setMaxResults(1)
//        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)
//        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
//        try {
//            animalClassifier = ImageClassifier.createFromFileAndOptions(context, modelName, optionsBuilder.build())
//        } catch (e: IllegalStateException) {
//            println("TFLite failed to load model with error: " + e.message)
//        }
//    }
    public fun run (input : Bitmap) : IntArray {
        val refitImage = Bitmap.createScaledBitmap(input, 320, 320, false)
         val imageBytes = convertBitmapToByteArray(refitImage);
//        val hallo = 3
//        val fileDescriptor = assetManager.openFd(modelName)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        val filemap = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

//        val interpereter = Interpreter(filemap)
//        val labels = arrayOf("aooke")
        GlobalScope.launch {
            try {
                val response = sendPostRequest(imageBytes)
                // Handle the response data
                val responseData = response.body?.string()
                println(responseData)
                // Use responseData as needed
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

            //  val interpreterOptions = Interpreter.Options().setNumThreads(numThreads)
//        interpreterOptions.addDelegate(GpuDelegate())
//        val f = FileUtil.loadMappedFile( this.context, modelName )
//        var interpreter = Interpreter(FileUtil.loadMappedFile( this.context, modelName), interpreterOptions)
//        var tensorImage = TensorImage.fromBitmap(input)
//        tensorImage = inputImageProcessorQuantized.process( tensorImage )
//        val confidenceScores = TensorBuffer.createFixedSize( confidenceScoresTensorShape , DataType.FLOAT32 )
//        val classes = TensorBuffer.createFixedSize( classesTensorShape , DataType.FLOAT32 )
//        val outputs = mapOf(
//            0 to classes.buffer ,
//            1 to confidenceScores.buffer
//        )
//        interpreter.runForMultipleInputsOutputs( arrayOf(tensorImage.buffer), outputs )
        return classesTensorShape
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
            .url("http://IP_ADDRESS_HERE/api/process_image/")
            .post(requestBody)
            .build()

        return client.newCall(request).execute()
    }

}