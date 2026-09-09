package com.ssajudn.bareuang.data.service

import com.ssajudn.bareuang.domain.error.AppException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReceiptAiHttpClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ReceiptAiHttpClient

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        client = ReceiptAiHttpClient(
            server.url("/api/parse-receipt").toString(),
            OkHttpClient.Builder().connectTimeout(500, TimeUnit.MILLISECONDS).readTimeout(500, TimeUnit.MILLISECONDS).build(),
        )
    }

    @After fun tearDown() = server.shutdown()

    @Test fun successResponse_isParsed() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"merchant\":\"Shop\",\"total\":12000}"))
        val result = client.execute("data:image/jpeg;base64,abc", "install-1")
        assertTrue(result.isSuccess)
        assertEquals("Shop", result.getOrThrow().merchant)
        assertEquals(12000L, result.getOrThrow().total)
        assertEquals("install-1", server.takeRequest().getHeader("X-Bareuang-Installation-Id"))
    }

    @Test fun malformedResponse_isDataError() = assertStatus(200, "not-json", AppException.DataException::class.java)
    @Test fun unauthorized_isDataError() = assertStatus(401, "{\"error\":\"unauthorized\"}", AppException.DataException::class.java)
    @Test fun payloadTooLarge_isDataError() = assertStatus(413, "{}", AppException.DataException::class.java)
    @Test fun rateLimited_isDataError() = assertStatus(429, "{}", AppException.DataException::class.java)
    @Test fun providerUnavailable_isDataError() = assertStatus(503, "{}", AppException.DataException::class.java)

    @Test fun readTimeout_isNetworkError() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{\"merchant\":\"Shop\"}")
                .setBodyDelay(2, TimeUnit.SECONDS),
        )
        val result = client.execute("image", "install")
        assertTrue(result.exceptionOrNull() is AppException.NetworkException)
    }

    @Test fun connectionRefused_isNetworkError() {
        server.shutdown()
        val result = client.execute("image", "install")
        assertTrue(result.exceptionOrNull() is AppException.NetworkException)
    }

    private fun assertStatus(code: Int, body: String, expected: Class<*>) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
        val error = client.execute("image", "install").exceptionOrNull()
        assertTrue("Expected ${expected.simpleName}, got $error", expected.isInstance(error))
    }
}
