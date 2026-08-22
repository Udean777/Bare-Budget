package com.ssajudn.barebudget.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.utils.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult
    data class Error(val message: String) : AuthResult
    object Cancelled : AuthResult
}

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: UserSessionManager,
    private val firebaseAuth: FirebaseAuth,
    private val database: com.ssajudn.barebudget.data.local.room.AppDatabase
) {
    private val credentialManager = CredentialManager.create(context)

    // Dynamically provided from AppConfig / BuildConfig
    private val webClientId = AppConfig.webClientId

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Google Sign-In using Android Credential Manager (Modern standard)
     */
    suspend fun signInWithGoogle(): AuthResult = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Authenticate to Firebase with Google IdToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val activeUser = firebaseAuth.currentUser

                val firebaseUser: FirebaseUser? = if (activeUser != null && activeUser.isAnonymous) {
                    try {
                        // Directly upgrade/link anonymous session to Google credentials
                        val linkResult = activeUser.linkWithCredential(authCredential).await()
                        linkResult.user
                    } catch (e: Exception) {
                        // If Google account already exists as a separate user, sign in to it
                        val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                        authResult.user
                    }
                } else {
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    authResult.user
                }

                if (firebaseUser != null) {
                    sessionManager.startUserSession(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: "User"
                    )
                    AuthResult.Success(firebaseUser)
                } else {
                    AuthResult.Error("Failed to retrieve Firebase user")
                }
            } else {
                AuthResult.Error("Unsupported credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            AuthResult.Error(e.localizedMessage ?: "Credential retrieval error")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Authentication failed: ${e.message}")
        }
    }

    /**
     * Anonymous Guest Sign-In via Firebase Auth
     */
    suspend fun signInAnonymously(): AuthResult = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                sessionManager.startGuestSession()
                // Update userId to match Firebase Anonymous UID
                sessionManager.userId = firebaseUser.uid
                AuthResult.Success(firebaseUser)
            } else {
                AuthResult.Error("Anonymous sign-in failed")
            }
        } catch (e: Exception) {
            // Fallback to local guest if network is completely down
            sessionManager.startGuestSession()
            AuthResult.Error("Continuing in offline guest mode: ${e.localizedMessage}")
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            // ponytail: clear local cache to prevent cross-account leakage
            try { database.clearAllTables() } catch (_: Exception) {}
            sessionManager.clearSession()
        } catch (e: Exception) {
            try { database.clearAllTables() } catch (_: Exception) {}
            sessionManager.clearSession()
        }
    }
}
