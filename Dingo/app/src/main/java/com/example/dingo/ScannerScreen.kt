package com.example.dingo

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.graphics.Rect
import android.media.Image
import android.graphics.YuvImage
import android.graphics.ImageFormat
import android.graphics.BitmapFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import android.graphics.Matrix
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel() ){
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Checks if the app has camera permissions
    if (cameraPermissionState.status.isGranted) {
        val scannerState: ScannerState by viewModel.state.collectAsStateWithLifecycle()

        ScannerPreview(
            onPhotoCaptured = viewModel::onPhotoCaptured
        )
        scannerState.capturedImage?.let { capturedImage:Bitmap->
            CapturedImageBitmapDialog(
                capturedImage = capturedImage,
                onDismissRequest = viewModel::onCapturedPhotoConsumed
            )
        }
    } else {
        NoPermission(cameraPermissionState::launchPermissionRequest)
    }

}

@Composable
private fun CapturedImageBitmapDialog(
    capturedImage: Bitmap,
    onDismissRequest: () -> Unit
) {
    val capturedImageBitmap: ImageBitmap = remember { capturedImage.asImageBitmap() }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Image(
            bitmap = capturedImageBitmap,
            contentDescription = "Captured Image"
        )
    }
}

//fun Image.toBitmap(): Bitmap {
//    val yBuffer = planes[0].buffer // Y
//    val vuBuffer = planes[2].buffer // VU
//
//    val ySize = yBuffer.remaining()
//    val vuSize = vuBuffer.remaining()
//
//    val nv21 = ByteArray(ySize + vuSize)
//
//    yBuffer.get(nv21, 0, ySize)
//    vuBuffer.get(nv21, ySize, vuSize)
//
//    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
//    val out = ByteArrayOutputStream()
//    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
//    val imageBytes = out.toByteArray()
//    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//}

fun ImageProxy.convertImageToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-rotationDegrees.toFloat())
        postScale(-1f, -1f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

@Composable
private fun ScannerPreview(
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }


    Scaffold(modifier = Modifier.fillMaxSize(),
//        floatingActionButtonPosition =  FabPosition.,
        floatingActionButton = {
            FloatingActionButton (
                modifier = Modifier
                    .padding(all = 128.dp),
                onClick = {
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val correctedBitmap: Bitmap = image.convertImageToBitmap().rotateBitmap(image.imageInfo.rotationDegrees)

                            onPhotoCaptured(correctedBitmap)

                            image.close()
                        }
                    })
                }
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = "Take picture"
                )
            }
        }) { innerPadding: PaddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                PreviewView(context).apply {
//                    setBackgroundColor(Color(0xFFD0BCFF))
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )
    }
}