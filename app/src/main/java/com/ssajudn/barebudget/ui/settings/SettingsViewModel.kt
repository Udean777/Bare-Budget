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
    private val sessionManager: UserSessionManager = UserSessionManager(context),
    private val repository: com.ssajudn.barebudget.data.repository.BudgetRepository = com.ssajudn.barebudget.data.repository.BudgetRepository()
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
            val previousGuestUserId = sessionManager.userId
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authManager.signInWithGoogle()) {
                is AuthResult.Success -> {
                    // Automatically migrate all guest data (transactions, bills, budgets) to Google Account UID
                    if (previousGuestUserId.isNotBlank() && previousGuestUserId != result.user.uid) {
                        repository.migrateGuestData(previousGuestUserId)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadUserProfile()
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully connected! All previous transactions were migrated to your Google account."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is AuthResult.Cancelled -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private val backupManager = com.ssajudn.barebudget.data.local.BackupRestoreManager(context)

    fun exportBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = backupManager.exportBackupToUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Backup berhasil diekspor ke file! Simpan file ini untuk restore kapan saja."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal mengekspor backup: ${result.exceptionOrNull()?.localizedMessage}"
                )
            }
        }
    }

    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = backupManager.importBackupFromUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _uiState.value = _uiState.value.copy(
                    successMessage = "Berhasil memulihkan $count data dari file backup!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memulihkan backup. Pastikan format file benar: ${result.exceptionOrNull()?.localizedMessage}"
                )
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
