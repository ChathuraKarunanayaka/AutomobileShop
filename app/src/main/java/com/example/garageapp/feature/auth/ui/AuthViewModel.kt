package com.example.garageapp.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garageapp.domain.model.User
import com.example.garageapp.domain.usecase.GetCurrentUserUseCase
import com.example.garageapp.domain.usecase.SignInUseCase
import com.example.garageapp.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val signIn: SignInUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        getCurrentUser().onEach { user ->
            if (user != null) {
                _uiState.value = AuthUiState.Success(user)
            } else {
                _uiState.value = AuthUiState.Idle
            }
        }.launchIn(viewModelScope)
    }

    fun signIn(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            // By explicitly calling this.signIn.invoke(), we ensure we are
            // calling the UseCase and not recursing into this function.
            val result = this@AuthViewModel.signIn.invoke(email, password)

            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { signOut() }
        _uiState.value = AuthUiState.Idle
    }
}
