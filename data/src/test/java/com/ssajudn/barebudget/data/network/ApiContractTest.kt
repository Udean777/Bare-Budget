package com.ssajudn.barebudget.data.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.ssajudn.barebudget.data.network.dto.WalletDto
import com.ssajudn.barebudget.data.network.dto.TransactionDto
import com.ssajudn.barebudget.data.network.dto.CreateWalletRequestDto
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(ApiService::class.java)
    }
    @After fun tearDown() { server.shutdown() }

    @Test fun walletDto_roundTrip_snakeCase() {
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        val dto = WalletDto(id="w1", userId="u1", name="Dompet", balance=1000, colorHex="#fff", iconName="wallet", createdAt="2026-01-01")
        val json = gson.toJson(dto)
        assertTrue(json.contains("color_hex"))
        assertTrue(json.contains("icon_name"))
        assertTrue(json.contains("user_id"))
        val parsed = gson.fromJson(json, WalletDto::class.java)
        assertEquals(dto, parsed)
        assertEquals("Dompet", parsed.toDomain().name)
    }

    @Test fun transactionDto_snakeCase() {
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        val json = """{"id":"t1","amount":5000,"type":"EXPENSE","wallet_id":"w1","to_wallet_id":null,"category":"FOOD","merchant":"Makan","date":"2026-01-01","notes":null,"receipt_url":null}"""
        val dto = gson.fromJson(json, TransactionDto::class.java)
        assertEquals("w1", dto.walletId)
        assertEquals("FOOD", dto.category)
        assertEquals("EXPENSE", dto.type)
    }

    @Test fun createWallet_request_snakeCase() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"id":"w1","user_id":"u1","name":"Tes","balance":0,"color_hex":"#000","icon_name":"wallet","created_at":"2026-01-01"}""").setResponseCode(200))
        server.start()
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
        val api2 = Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create(gson)).build().create(ApiService::class.java)
        kotlinx.coroutines.runBlocking {
            val resp = api2.createWallet(CreateWalletRequestDto("Tes",0,"#000","wallet"))
            assertTrue(resp.isSuccessful)
            val req = server.takeRequest()
            val body = req.body.readUtf8()
            assertTrue(body.contains("color_hex"))
            assertTrue(body.contains("icon_name"))
        }
        server.shutdown()
    }

    @Test fun wallets_endpoint_parses_list() {
        server.enqueue(MockResponse().setBody("""[{"id":"w1","user_id":"u1","name":"A","balance":100,"color_hex":"#000","icon_name":"wallet","created_at":"2026-01-01"}]""").setResponseCode(200))
        kotlinx.coroutines.runBlocking {
            val resp = api.getWallets()
            assertTrue(resp.isSuccessful)
            assertEquals(1, resp.body()!!.size)
            assertEquals("A", resp.body()!![0].toDomain().name)
        }
    }
}
