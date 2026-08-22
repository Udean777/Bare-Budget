package com.ssajudn.barebudget.data.network

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.content.Context
import com.ssajudn.barebudget.data.i18n.AndroidLanguageProvider
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LanguageInterceptorTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("id"))
    }
    @After fun tearDown() {
        server.shutdown()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @Test fun sendsAcceptLanguage_id() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        val ctx = mockk<Context>(relaxed = true)
        val provider = AndroidLanguageProvider(ctx)
        val client = OkHttpClient.Builder().addInterceptor(LanguageInterceptor(provider)).build()
        client.newCall(Request.Builder().url(server.url("/test")).build()).execute().close()
        val header = server.takeRequest().getHeader("Accept-Language")
        assertEquals("id", header)
        assertFalse(header!!.contains("-"))
    }

    @Test fun sendsAcceptLanguage_en() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        val ctx = mockk<Context>(relaxed = true)
        val provider = AndroidLanguageProvider(ctx)
        val client = OkHttpClient.Builder().addInterceptor(LanguageInterceptor(provider)).build()
        client.newCall(Request.Builder().url(server.url("/test")).build()).execute().close()
        assertEquals("en", server.takeRequest().getHeader("Accept-Language"))
    }
}
