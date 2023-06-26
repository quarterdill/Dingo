package com.example.dingo.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dingo.model.Post
import com.example.dingo.model.service.ClassroomService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClassroomViewModel
@Inject
constructor(
    private val classroomService: ClassroomService,
) : ViewModel() {


    private fun getClassroomFeed(classroomId: String): LiveData<MutableList<Post>?> {
        TODO("Not yet implemented")
    }
}