package com.example.skillmate.ui.profile

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
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile(userId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = userRepository.getUser(userId)
            _isLoading.value = false
            result.onSuccess { _profile.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    fun saveProfile(profile: UserProfile) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = userRepository.createOrUpdateUser(profile)
            _isLoading.value = false
            result.onSuccess { _profile.value = profile }
            result.onFailure { _error.value = it.message }
        }
    }
} 