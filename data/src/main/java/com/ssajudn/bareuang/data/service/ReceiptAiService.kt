package com.ssajudn.bareuang.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.ssajudn.bareuang.data.BuildConfig
import com.ssajudn.bareuang.domain.error.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

typealias AiParsedReceipt = com.ssajudn.bareuang.domain.port.AiParsedReceipt

private data class ProxyResponse(
    @SerializedName("merchant") val merchant: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("total") val total: Long?,
    @SerializedName("category") val category: String?,
    @SerializedName("items") val items: List<String>?,
    @SerializedName("raw_text") val rawText: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("detail") val detail: String?,
)

private const val GENERIC_OCR_ERROR = "Scan struk gagal. Silakan coba lagi."

@Singleton
class ReceiptAiService @Inject constructor(
    @ApplicationContext private val context: Context,
) : com.ssajudn.bareuang.domain.port.ReceiptAiPort {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val installationId: String by lazy {
        val prefs = context.getSharedPreferences("bareuang_client_identity", Context.MODE_PRIVATE)
        prefs.getString("installation_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("installation_id", it).apply()
        }
    }

    suspend fun parseReceiptImage(uri: Uri): Result<AiParsedReceipt> = parseReceiptImage(uri.toString())

    override suspend fun parseReceiptImage(uri: String): Result<AiParsedReceipt> = withContext(Dispatchers.IO) {
        val parsedUri = Uri.parse(uri)
        runCatching {
            val base64 = encodeImage(parsedUri)
            val reqJson = gson.toJson(mapOf("image_base64" to base64))
            val req = Request.Builder()
                .url(BuildConfig.PARSE_RECEIPT_URL)
                .header("X-Bareuang-Installation-Id", installationId)
                .post(reqJson.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val bodyStr = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                throw AppException.DataException(proxyErrorMessage(resp.code, bodyStr))
            }
            val pr = gson.fromJson(bodyStr, ProxyResponse::class.java)
                ?: throw AppException.DataException("Gagal memproses struk. Coba lagi.")
            if (pr.error != null) {
                throw AppException.DataException(
                    if (BuildConfig.DEBUG) pr.error?.takeIf { it.isNotBlank() } ?: GENERIC_OCR_ERROR
                    else GENERIC_OCR_ERROR,
                )
            }
            // Sanitize client-side (proxy already does, but never trust)
            val allowed = setOf("FOOD","SHOPPING","TRANSPORT","BILLS","ENTERTAINMENT","HEALTH","EDUCATION","SOCIAL","OTHER")
            val cat = pr.category?.trim()?.uppercase() ?: "OTHER"
            AiParsedReceipt(
                merchant = pr.merchant?.trim()?.take(100) ?: "",
                date = pr.date?.trim()?.take(10) ?: "",
                total = maxOf(0L, pr.total ?: 0L),
                category = if (cat in allowed) cat else "OTHER",
                items = pr.items?.take(30)?.map { it.take(100) } ?: emptyList(),
                rawText = pr.rawText?.trim()?.take(4000) ?: "",
            )
        }.recoverCatching { e ->
            android.util.Log.e("ReceiptAiService", "parse failed", e)
            when (e) {
                is AppException -> throw e
                is IOException -> throw AppException.NetworkException(
                    if (BuildConfig.DEBUG) e.message?.takeIf { it.isNotBlank() } ?: "Koneksi bermasalah. Cek internet."
                    else GENERIC_OCR_ERROR,
                    e,
                )
                is IllegalStateException, is IllegalArgumentException -> throw AppException.DataException(
                    if (BuildConfig.DEBUG) e.message?.takeIf { it.isNotBlank() } ?: GENERIC_OCR_ERROR
                    else GENERIC_OCR_ERROR,
                    e,
                )
                else -> throw AppException.UnknownError(cause = e)
            }
        }
    }

    private fun proxyErrorMessage(statusCode: Int, body: String): String {
        if (!BuildConfig.DEBUG) return GENERIC_OCR_ERROR
        val serverMessage = runCatching { gson.fromJson(body, ProxyResponse::class.java) }
            .getOrNull()
            ?.let { response ->
                response.error?.takeIf { it.isNotBlank() }
                    ?: response.detail?.takeIf { it.isNotBlank() }
            }
        return serverMessage ?: "OCR server mengembalikan HTTP $statusCode."
    }

    private fun encodeImage(uri: Uri): String {
        // Guard size: reject > 5MB before decode (prevent OOM)
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            if (pfd.statSize > 5 * 1024 * 1024) throw AppException.DataException("Ukuran gambar terlalu besar (maks 5MB)")
        }
        val input = context.contentResolver.openInputStream(uri)
            ?: throw AppException.DataException("Gagal membuka gambar")
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, opts)
        input.close()
        // Reject absurd dimensions (prevent OOM on decode)
        if (opts.outWidth > 8000 || opts.outHeight > 8000) throw AppException.DataException("Dimensi gambar terlalu besar")
        // Re-open for actual decode
        val input2 = context.contentResolver.openInputStream(uri)
            ?: throw AppException.DataException("Gagal membuka gambar")
        // Downscale to ~1024px max side to keep body < 3MB and reduce cost (vision resizes to 800 anyway)
        val maxSide = 1024
        val sample = run {
            val w = opts.outWidth.takeIf { it > 0 } ?: maxSide
            val h = opts.outHeight.takeIf { it > 0 } ?: maxSide
            val max = maxOf(w, h)
            var s = 1
            while (max / s > maxSide) s *= 2
            s
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = BitmapFactory.decodeStream(input2, null, decodeOpts)
            ?: throw AppException.DataException("Gagal membuka gambar")
        input2.close()

        // Further scale if still > maxSide
        val scale = maxSide.toFloat() / maxOf(bmp.width, bmp.height).toFloat()
        if (scale < 1f) {
            val nw = (bmp.width * scale).toInt()
            val nh = (bmp.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true)
            if (scaled !== bmp) bmp.recycle()
            bmp = scaled
        }
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
        bmp.recycle()
        val bytes = out.toByteArray()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }
}
