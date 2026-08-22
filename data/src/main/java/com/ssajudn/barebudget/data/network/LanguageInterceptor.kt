package com.ssajudn.barebudget.data.network

import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import com.ssajudn.barebudget.data.i18n.AndroidLanguageProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injects the `Accept-Language` header into outgoing HTTP requests based on the
 * app's active locale preference.
 */
@Singleton
class LanguageInterceptor @Inject constructor(
    private val languageProvider: AndroidLanguageProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val languageTag = languageProvider.getLanguage()
        val request = chain.request().newBuilder()
            .header("Accept-Language", languageTag)
            .build()
        return chain.proceed(request)
    }
}
