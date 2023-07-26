package com.example.dingo.model.service.impl

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.ImageInternalStorageService
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class ImageInternalStorageServiceImpl
@Inject
constructor() :
    ImageInternalStorageService {
    override suspend fun saveImage(entryName: String, image: Bitmap, context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val fileName = "${entryName}_${dateFormat.format(Date())}.png"

        val directory = File(context.filesDir, "Users/temp/images/$entryName/")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)

        // Launch a coroutine to perform the file saving in the background

        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            println("image saved")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }


}