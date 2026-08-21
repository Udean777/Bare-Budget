package com.ssajudn.barebudget.data.network

import com.ssajudn.barebudget.BuildConfig
import com.ssajudn.barebudget.utils.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holder for the authenticated HTTP client and Retrofit [ApiService].
 *
 * Previously this was a mutable `object` with `var authToken` — an anti-pattern
 * that made the token a race-prone global and impossible to swap in tests.
 * It is now a `@Singleton` class constructed by [com.ssajudn.barebudget.di.NetworkModule]
 * and receives its auth header via [AuthInterceptor], which reads the current
 * token from [com.ssajudn.barebudget.data.local.UserSessionManager] on every
 * request.
 */
@Singleton
class ApiClient @Inject constructor(
    private val authInterceptor: AuthInterceptor
) {

    val BASE_URL: String = AppConfig.baseUrl

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (AppConfig.enableHttpLogging) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
