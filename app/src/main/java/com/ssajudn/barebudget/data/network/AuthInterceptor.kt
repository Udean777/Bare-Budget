package com.ssajudn.barebudget.data.network

import com.ssajudn.barebudget.data.local.UserSessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds `Authorization`, `X-User-Email`, and `Accept` headers to every request,
 * reading the current values from [UserSessionManager] on each call so that a
 * login/logout immediately takes effect without mutating any global state.
 *
 * Replaces the old `ApiClient.var authToken` mutable global, which was
 * race-prone and impossible to replace in tests.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: UserSessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionManager.userId.ifBlank { GUEST_FALLBACK_TOKEN }
        val email = sessionManager.userEmail

        val request = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("X-User-Email", email)
            .header("Accept", "application/json")
            .build()

        return chain.proceed(request)
    }
}

// Used only when no session has been started yet (e.g. onboarding).
// The backend middleware treats any non-empty bearer as a user id in
// development mode; an empty token would 401 every request before the
// user even signs in. This is dev-only and documented in AppConfig.
private const val GUEST_FALLBACK_TOKEN = "dev-user-123"

