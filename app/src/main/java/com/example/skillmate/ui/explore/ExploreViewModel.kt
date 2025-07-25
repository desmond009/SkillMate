package com.example.skillmate.ui.explore

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
class ExploreViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUsers(userId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val userResult = userRepository.getUser(userId)
            userResult.onSuccess { _currentUserProfile.value = it }
            userResult.onFailure { _error.value = it.message }
            val allResult = userRepository.getAllPublicUsers(userId)
            _isLoading.value = false
            allResult.onSuccess { _allUsers.value = it }
            allResult.onFailure { _error.value = it.message }
        }
    }
} 