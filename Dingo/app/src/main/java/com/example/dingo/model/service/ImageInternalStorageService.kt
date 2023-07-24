package com.example.dingo.model.service

import android.content.Context
import android.graphics.Bitmap
import com.example.dingo.model.DingoDex

interface ImageInternalStorageService {
    suspend fun saveImage(entryName: String, image: Bitmap, context: Context)
    // Dev only

}