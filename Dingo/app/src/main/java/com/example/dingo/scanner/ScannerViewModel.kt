package com.example.dingo.scanner

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.common.SessionInfo
import com.example.dingo.AnimalDetectionModel
import com.example.dingo.common.addNewEntryToTrip
import com.example.dingo.common.addPictureToTrip
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.DingoDexStorageService
import com.example.dingo.model.service.ImageInternalStorageService
import com.example.dingo.model.service.UserService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ScannerViewModel
@Inject
constructor(
    private val accService: AccountService,
    private val userService: UserService,
    private val dingoDexEntryService: DingoDexEntryService,
    private val dingoDexStorageService: DingoDexStorageService,
    private val imageInternalStorageService: ImageInternalStorageService,
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()
    var isLoading = MutableLiveData<Boolean>(false)

    fun scanImage(bitmap: Bitmap, context: Context, saveAsDefault: Boolean, saveStorage: Boolean, animal: Boolean, callBack: (String) -> Unit) {
        val animalDetectionModel = AnimalDetectionModel(context)
        val prediction = animalDetectionModel.run( bitmap , callBack, saveAsDefault, saveStorage,animal, SessionInfo.lastLocation, this::savePicture, this::addEntry)
        updateCapturedPhotoState(bitmap)
    }
    fun onPhotoCaptured(bitmap: Bitmap) {
        updateCapturedPhotoState(bitmap)
    }

    fun onCapturedPhotoConsumed() {
        updateCapturedPhotoState(null)
    }

    fun addEntry(entryName: String) {
        viewModelScope.launch {
            isLoading.value = true
            val entries = dingoDexEntryService.getEntry(SessionInfo.currentUserID, entryName)
            addNewEntryToTrip(entryName)
            if (entries.isEmpty()) {
                val dingoDex = dingoDexStorageService.findDingoDexItem(entryName)
                if (dingoDex != null) {
                    userService.updateDingoDex(dingoDex.id, dingoDex.is_fauna)
                    dingoDexEntryService.addNewEntry(dingoDex)
                }
            } else {
                // Should only have 1 entry for each animal/plant
                var entry = entries[0]
                entry.numEncounters++
                // TODO: update location
                entry.location = "Waterloo"
                dingoDexEntryService.updateEntry(entry)
            }
            isLoading.value = false
        }
    }

    fun savePicture(entryName: String, image: Bitmap, saveAsDefault: Boolean, saveImage: Boolean, location: LatLng?, context: Context) {
        viewModelScope.launch {
            if (saveImage) {
                imageInternalStorageService.saveImage(entryName, image, context)
                val imagePath = dingoDexEntryService.addPicture(entryName, image)
                println("savePicture path is $imagePath")
                var entry: DingoDexEntry? = null
                if (imagePath != "") {
                    val entries = dingoDexEntryService.getEntry(SessionInfo.currentUserID, entryName)
                    if (entries.isNotEmpty()) {
                        // Should only have 1 entry for each animal/plant
                        entry = entries[0]
                        entry.pictures.add(imagePath)
                        dingoDexEntryService.updateEntry(entry)
                    }
                }
                if (saveAsDefault) {
                    var result = false
                    addPictureToTrip(imagePath, location)
                    if (imagePath != "" && entry != null) {
                        entry.displayPicture = imagePath
                        dingoDexEntryService.updateEntry(entry)
                    }
                } else if (SessionInfo.trip != null ) {
                    addPictureToTrip(imagePath, location)
                }
            }

        }
    }

    private fun updateCapturedPhotoState(updatedPhoto: Bitmap?) {
        _state.value.capturedImage?.recycle()
        _state.value = _state.value.copy(capturedImage = updatedPhoto)
    }

    override fun onCleared() {
        _state.value.capturedImage?.recycle()
        super.onCleared()
    }
}