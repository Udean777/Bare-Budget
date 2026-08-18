package com.ssajudn.barebudget.utils

import com.ssajudn.barebudget.BuildConfig

/**
 * Application-level environment configuration helper.
 * Provides clean access to build flags without scattering BuildConfig across business logic.
 */
object AppConfig {

    val isDebug: Boolean
        get() = BuildConfig.DEBUG

    val baseUrl: String
        get() = BuildConfig.BASE_URL

    val isProduction: Boolean
        get() = !BuildConfig.DEBUG

    // Feature Flags / Environment Specific Options
    val enableHttpLogging: Boolean
        get() = isDebug

    val webClientId: String
        get() = BuildConfig.WEB_CLIENT_ID
}
