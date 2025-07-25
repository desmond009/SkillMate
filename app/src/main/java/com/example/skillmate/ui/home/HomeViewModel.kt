package com.example.skillmate.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmate.data.UserProfile
import com.example.skillmate.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUsers(userId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val userResult = userRepository.getUser(userId)
            userResult.onSuccess { _currentUser.value = it }
            userResult.onFailure { _error.value = it.message }
            val allResult = userRepository.getAllPublicUsers(userId)
            _isLoading.value = false
            allResult.onSuccess { _allUsers.value = it }
            allResult.onFailure { _error.value = it.message }
        }
    }
} 