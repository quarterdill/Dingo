package com.example.dingo.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.graphics.BitmapFactory
import androidx.compose.ui.unit.dp
import android.graphics.Matrix
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FabPosition
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dingo.R
import com.example.dingo.common.SessionInfo
import com.example.dingo.UIConstants
import com.example.dingo.ui.theme.color_light_transparent
import com.example.dingo.ui.theme.color_on_primary
import com.example.dingo.ui.theme.color_on_secondary
import com.example.dingo.ui.theme.color_primary
import com.example.dingo.ui.theme.color_secondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.type.LatLng
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    animalCallback: (String) -> Unit
) {
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
                onDismissRequest = viewModel::onCapturedPhotoConsumed,
                animalCallback = animalCallback
            )
        }
    } else {
        NoPermission(cameraPermissionState::launchPermissionRequest)
    }

}

@SuppressLint("MissingPermission")
@Composable
private fun CapturedImageBitmapDialog(
    capturedImage: Bitmap,
    onDismissRequest: () -> Unit,
    animalCallback: (String) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val capturedImageBitmap: ImageBitmap = remember { capturedImage.asImageBitmap() }
    val isLoading = viewModel.isLoading.observeAsState()
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    //viewModel.addEntry("Dummy Data")
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Box (
            // TODO: might need to change height property so it matches imges exactly
            modifier = Modifier.width(capturedImageBitmap.width.dp).height(capturedImageBitmap.height.dp/3),
            contentAlignment = Alignment.Center
        ){
            if (isLoading.value!!) {
                CircularProgressIndicator(
                    color = color_secondary
                )
            } else {
                Box {
                    var setDefaultPicture by remember { mutableStateOf(true) }
                    var savePicture by remember { mutableStateOf( true )}
                    Image(
                        bitmap = capturedImageBitmap,
                        contentDescription = "Captured Image"
                    )
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier.size(24.dp),
                            onClick = onDismissRequest,
                            colors =
                                IconButtonDefaults.iconButtonColors(containerColor = color_light_transparent, color_primary)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                "close",
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(UIConstants.SMALL_PADDING),
                        verticalArrangement = Arrangement.spacedBy(UIConstants.SMALL_PADDING),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = color_light_transparent,
                                    shape = RoundedCornerShape(5.dp),
                                )
                                .padding(horizontal = UIConstants.SMALL_PADDING),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = setDefaultPicture,
                                onCheckedChange = { setDefaultPicture = it },
                                colors =
                                CheckboxDefaults.colors(color_primary, checkmarkColor = color_on_primary)
                            )
                            Text(
                                "Set as Default",
                                color = color_primary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = color_light_transparent,
                                    shape = RoundedCornerShape(5.dp),
                                )
                                .padding(horizontal = UIConstants.SMALL_PADDING),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = savePicture,
                                onCheckedChange = { savePicture = it },
                                colors =
                                CheckboxDefaults.colors(color_primary, checkmarkColor = color_on_primary)
                            )
                            Text(
                                "Save Image",
                                color = color_primary
                            )
                        }
                        Row (
                            horizontalArrangement = Arrangement.spacedBy(UIConstants.MEDIUM_PADDING),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = {
                                    viewModel.scanImage(
                                        capturedImage,
                                        context,
                                        setDefaultPicture,
                                        savePicture,
                                        true,
                                        animalCallback
                                    )
                                    onDismissRequest()
                                    // viewModel.savePicture("Dummy_Data", capturedImage, setDefaultPicture, context)
                                },
                                colors =
                                ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                            ) {
                                Text(text = "Scan Animal")
                            }
                            Button(
                                onClick = {
                                    viewModel.scanImage(
                                        capturedImage,
                                        context,
                                        setDefaultPicture,
                                        savePicture,
                                        false,
                                        animalCallback
                                    )
                                    onDismissRequest()
                                    // viewModel.savePicture("Dummy_Data", capturedImage, setDefaultPicture, context)
                                },
                                colors =
                                ButtonDefaults.buttonColors(containerColor = color_secondary, color_on_secondary)
                            ) {
                                Text(text = "Scan Plant")
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    viewModel: ScannerViewModel = hiltViewModel(),
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }


    Scaffold(modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition =  FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton (
                onClick = {
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val correctedBitmap: Bitmap = image.convertImageToBitmap().rotateBitmap(image.imageInfo.rotationDegrees)

                            onPhotoCaptured(correctedBitmap)

                            image.close()
                        }
                    })
//                    viewModel.addEntry("Dummy Fauna")
                },
                containerColor = color_on_primary,
                contentColor = color_primary,
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.baseline_camera_alt_24),
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
