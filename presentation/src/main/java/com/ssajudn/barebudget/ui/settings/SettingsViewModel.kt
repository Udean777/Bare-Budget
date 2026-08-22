package com.ssajudn.barebudget.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ssajudn.barebudget.data.auth.AuthManager
import com.ssajudn.barebudget.data.auth.AuthResult
import com.ssajudn.barebudget.data.local.BackupRestoreManager
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.domain.repository.MigrationRepository
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.error.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import javax.inject.Inject

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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val sessionManager: UserSessionManager,
    private val repository: MigrationRepository,
    private val backupManager: BackupRestoreManager
) : ViewModel() {
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


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
            _operation.value = OperationState.Loading
            when (val result = authManager.signInWithGoogle()) {
                is AuthResult.Success -> {
                    if (previousGuestUserId.isNotBlank() && previousGuestUserId != result.user.uid) {
                        repository.migrateGuestData(previousGuestUserId)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadUserProfile()
                    _uiState.value = _uiState.value.copy(successMessage = "Successfully connected! All previous transactions were migrated to your Google account.")
                    _operation.value = OperationState.Success()
                    _effect.send(UiEffect.ShowSnackbar("Terhubung ke Google"))
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                    _operation.value = OperationState.Error(result.message)
                    _effect.send(UiEffect.ShowSnackbar(result.message))
                }
                is AuthResult.Cancelled -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _operation.value = OperationState.Idle
                }
            }
        }
    }

    fun exportBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.exportBackupToUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Backup berhasil diekspor ke file! Simpan file ini untuk restore kapan saja.")
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbar("Backup diekspor"))
            } else {
                val msg = "Gagal mengekspor backup: ${result.exceptionOrNull()?.localizedMessage}"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _operation.value = OperationState.Error(msg)
                _effect.send(UiEffect.ShowSnackbar(msg))
            }
        }
    }

    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _operation.value = OperationState.Loading
            val result = backupManager.importBackupFromUri(uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _uiState.value = _uiState.value.copy(successMessage = "Berhasil memulihkan $count data dari file backup!")
                _operation.value = OperationState.Success()
                _effect.send(UiEffect.ShowSnackbar("Restore berhasil"))
            } else {
                val msg = "Gagal memulihkan backup. Pastikan format file benar: ${result.exceptionOrNull()?.localizedMessage}"
                _uiState.value = _uiState.value.copy(errorMessage = msg)
                _operation.value = OperationState.Error(msg)
                _effect.send(UiEffect.ShowSnackbar(msg))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _operation.value = OperationState.Loading
            authManager.signOut()
            _uiState.value = _uiState.value.copy(isLoading = false, isSignedOut = true)
            _operation.value = OperationState.Success()
            _effect.send(UiEffect.Navigate("splash"))
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
