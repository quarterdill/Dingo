package com.example.dingo.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.Achievement
import com.example.dingo.model.User
import com.example.dingo.model.service.ClassroomService
import com.example.dingo.model.service.PostService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val userService: UserService,
) : ViewModel() {
    fun getNumFlora(user: User): Int {
        return user.uncollectedFlora.size
    }
    fun getNumFauna(user: User): Int {
        return user.uncollectedFauna.size
    }
    fun getAchievements(user: User): List<Achievement> {
        return emptyList()
    }

}