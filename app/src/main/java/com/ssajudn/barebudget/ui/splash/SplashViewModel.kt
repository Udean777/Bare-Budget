package com.ssajudn.barebudget.ui.splash

import androidx.lifecycle.ViewModel
import com.ssajudn.barebudget.data.local.UserSessionManager
import com.ssajudn.barebudget.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Determines the app's start destination from the persisted session.
 *
 * Replaces the previous `UserSessionManager(context).apply { initSession() }`
 * pattern in [SplashScreen] / [com.ssajudn.barebudget.ui.navigation.AppNavigation],
 * which constructed a *second* session manager instance separate from the
 * Hilt-provided singleton and mutated a global `ApiClient.authToken`.
 *
 * With Hilt, [UserSessionManager] is a singleton: the same instance is shared
 * by [com.ssajudn.barebudget.data.network.AuthInterceptor], the repositories,
 * and this ViewModel. No global mutation is needed because the interceptor
 * reads `userId` live on every request.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: UserSessionManager
) : ViewModel() {

    /** The route the app should navigate to after the splash animation. */
    fun computeStartDestination(): String = when {
        !sessionManager.isOnboardingCompleted -> Screen.Onboarding.route
        sessionManager.userId.isNotBlank() -> Screen.Dashboard.route
        else -> Screen.Auth.route
    }
}
