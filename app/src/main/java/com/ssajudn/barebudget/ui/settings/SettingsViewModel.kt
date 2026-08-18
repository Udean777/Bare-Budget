package com.ssajudn.barebudget.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ssajudn.barebudget.data.auth.AuthManager
import com.ssajudn.barebudget.data.auth.AuthResult
import com.ssajudn.barebudget.data.local.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isGuestMode: Boolean = false,
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    context: Context,
    private val authManager: AuthManager = AuthManager(context),
    private val sessionManager: UserSessionManager = UserSessionManager(context)
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user: FirebaseUser? = authManager.currentUser
        val isGuest = sessionManager.isGuestMode || (user != null && user.isAnonymous)
        val name = user?.displayName ?: sessionManager.userName.ifBlank { "User" }
        val email = user?.email ?: sessionManager.userEmail.ifBlank { "guest@barebudget.app" }
        val uid = user?.uid ?: sessionManager.userId

        _uiState.value = _uiState.value.copy(
            isGuestMode = isGuest,
            userId = uid,
            userEmail = email,
            userName = name
        )
    }

    fun linkWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authManager.signInWithGoogle()
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is AuthResult.Success -> {
                    loadUserProfile()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully connected with Google account!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is AuthResult.Cancelled -> { /* User cancelled */ }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authManager.signOut()
            _uiState.value = _uiState.value.copy(isLoading = false, isSignedOut = true)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
