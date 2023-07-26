package com.example.dingo.scanner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.graphics.Bitmap

data class ScannerState(
    val capturedImage: Bitmap? = null
)