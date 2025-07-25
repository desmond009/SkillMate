package com.example.skillmate.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmate.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email = MutableStateFlow("")
    var password = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        authRepository.currentUser.onEach { _currentUser.value = it }.launchIn(viewModelScope)
    }

    fun signIn() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signIn(email.value, password.value)
            _isLoading.value = false
            result.onFailure { _error.value = it.message }
        }
    }

    fun signUp() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authRepository.signUp(email.value, password.value)
            _isLoading.value = false
            result.onFailure { _error.value = it.message }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
} 