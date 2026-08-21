package com.ssajudn.barebudget.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.data.auth.AuthManager
import com.ssajudn.barebudget.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin wrapper around [AuthManager] for the onboarding/auth screens.
 *
 * Previously [AuthScreen] and [OnboardingScreen] each constructed their own
 * `AuthManager(context)` instance via `LocalContext`. With Hilt, [AuthManager]
 * is a singleton shared with [com.ssajudn.barebudget.ui.settings.SettingsViewModel],
 * so the same Firebase Auth state and CredentialManager is reused everywhere.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    fun signInWithGoogle(onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            onResult(authManager.signInWithGoogle())
        }
    }

    fun signInAnonymously(onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            onResult(authManager.signInAnonymously())
        }
    }
}
