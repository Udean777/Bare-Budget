package com.ssajudn.bareuang.data.service

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.ssajudn.bareuang.domain.error.AppException
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class ReceiptAiHttpResponse(
    @SerializedName("merchant") val merchant: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("total") val total: Long?,
    @SerializedName("category") val category: String?,
    @SerializedName("items") val items: List<String>?,
    @SerializedName("raw_text") val rawText: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("detail") val detail: String?,
)

/** Network-only OCR adapter; policy/sanitization stays in ReceiptAiService. */
class ReceiptAiHttpClient(
    private val endpoint: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    fun execute(imageBase64: String, installationId: String): Result<ReceiptAiHttpResponse> = runCatching {
        val request = Request.Builder()
            .url(endpoint)
            .header("X-Bareuang-Installation-Id", installationId)
            .post(gson.toJson(mapOf("image_base64" to imageBase64)).toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw AppException.DataException("OCR HTTP ${response.code}: $body")
            gson.fromJson(body, ReceiptAiHttpResponse::class.java)
                ?: throw AppException.DataException("OCR returned malformed response")
        }
    }.recoverCatching { error ->
        when (error) {
            is AppException -> throw error
            is IOException -> throw AppException.NetworkException(cause = error)
            is JsonParseException -> throw AppException.DataException("OCR returned malformed response", error)
            else -> throw AppException.UnknownError(cause = error)
        }
    }
}
