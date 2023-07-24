package com.example.dingo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dingo.model.service.AccountService
import com.example.dingo.model.service.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val repo: AccountService,
    private val userService: UserService
): ViewModel() {
    init {
        getAuthState()
    }

    fun getAuthState() = repo.getAuthState(viewModelScope)

    fun getUser() {
        viewModelScope.launch {
            userService.getCurrentUser()
        }
    }

    val isEmailVerified get() = repo.currentUser?.isEmailVerified ?: false

}