package com.example.dingo.scanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.DingoDexEntry
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.DingoDexEntryService
import com.example.dingo.model.service.DingoDexStorageService
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
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state = _state.asStateFlow()

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
            val entries = dingoDexEntryService.getEntry(entryName)
            if (entries.isEmpty()) {
                val dingoDex = dingoDexStorageService.findDingoDexItem(entryName)
                if (dingoDex != null) {
                    userService.updateDingoDex(dingoDex.id, dingoDex.isFauna)

                    dingoDexEntryService.addNewEntry(dingoDex)
                }
            } else {
                // Should only have 1 entry for each animal/plant
                var entry = entries[0]
                entry.numEncounters++
                // TODO: update location
                entry.location = ""
                dingoDexEntryService.updateEntry(entry)
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