package com.ssajudn.barebudget.data.error

import com.ssajudn.barebudget.domain.error.AppException
import retrofit2.Response
import java.io.IOException

/**
 * Data-layer mapper — jangan bocorkan HttpException/IOException ke domain/ui.
 * Parse errorBody -> AppException dengan pesan server bila ada.
 */
object ApiErrorParser {

    fun <T> parse(response: Response<T>): AppException {
        val raw = try {
            response.errorBody()?.string()?.take(300)
        } catch (_: Exception) {
            null
        }
        val msg = raw?.takeIf { it.isNotBlank() } ?: "Gagal memuat data (${response.code()})"
        return when (response.code()) {
            401, 403 -> AppException.AuthException(msg)
            in 500..599 -> AppException.NetworkException(msg)
            else -> AppException.DataException(msg)
        }
    }

    fun fromThrowable(e: Throwable): AppException = when (e) {
        is IOException -> AppException.NetworkException(e.message ?: "Koneksi terputus", e)
        is AppException -> e
        else -> AppException.UnknownError(e.message ?: "Terjadi kesalahan", e)
    }

    fun message(e: Throwable): String = when (e) {
        is AppException -> e.message ?: "Terjadi kesalahan"
        else -> e.localizedMessage ?: "Terjadi kesalahan"
    }
}
