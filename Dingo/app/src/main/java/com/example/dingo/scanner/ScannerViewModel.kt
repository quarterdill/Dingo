package com.example.dingo.scanner

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.DingoDexStorageService
import com.example.dingo.model.service.ImageInternalStorageService
import com.example.dingo.model.service.UserService
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

    fun onPhotoCaptured(bitmap: Bitmap) {
        // TODO: Process your photo, for example store it in the MediaStore
        // here we only do a dummy showcase implementation
        updateCapturedPhotoState(bitmap)
    }

    fun onCapturedPhotoConsumed() {
        updateCapturedPhotoState(null)
    }

    fun addEntry(entryName: String) {

        viewModelScope.launch {
            var result = false
            isLoading.value = true
            val entries = dingoDexEntryService.getEntry(entryName)
            if (entries.isEmpty()) {
                val dingoDex = dingoDexStorageService.findDingoDexItem(entryName)
                if (dingoDex != null) {
                    userService.updateDingoDex(dingoDex.id, dingoDex.isFauna)

                    result =dingoDexEntryService.addNewEntry(dingoDex)
                }
            } else {
                // Should only have 1 entry for each animal/plant
                var entry = entries[0]
                entry.numEncounters++
                // TODO: update location
                entry.location = ""
                result = dingoDexEntryService.updateEntry(entry)
            }
            isLoading.value = false
        }
    }

    fun savePicture(entryName: String, image: Bitmap, saveAsDefault: Boolean, context: Context) {
        viewModelScope.launch {
            imageInternalStorageService.saveImage("test", image, context)
            if (saveAsDefault) {

                    var result = false
                    val imagePath = dingoDexEntryService.addPicture(entryName, image)
                    if (imagePath != "") {
                        val entries = dingoDexEntryService.getEntry(entryName)
                        if (entries.isNotEmpty()) {
                            // Should only have 1 entry for each animal/plant
                            var entry = entries[0]
                            entry.displayPicture = imagePath
                            result = dingoDexEntryService.updateEntry(entry)
                        }
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