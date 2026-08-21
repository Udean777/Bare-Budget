package com.ssajudn.barebudget.domain.error

/**
 * Domain-level typed errors — tidak bocor HttpException/IOException ke ui.
 * Clean Architecture: error type milik domain, data map ke sini via ApiErrorParser.
 */
sealed class AppException(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    class NetworkException(
        message: String? = "Koneksi bermasalah, coba lagi",
        cause: Throwable? = null
    ) : AppException(message, cause)

    class AuthException(
        message: String? = "Sesi berakhir, silakan login kembali",
        cause: Throwable? = null
    ) : AppException(message, cause)

    class DataException(
        message: String? = "Data tidak valid",
        cause: Throwable? = null
    ) : AppException(message, cause)

    class SyncException(
        message: String? = "Gagal sinkronisasi",
        cause: Throwable? = null
    ) : AppException(message, cause)

    class UnknownError(
        message: String? = "Terjadi kesalahan tidak diketahui",
        cause: Throwable? = null
    ) : AppException(message, cause)
}

/**
 * Helper untuk ui: pesan spesifik per tipe (DIP — ui depend domain/error, bukan data).
 */
fun AppException.userMessage(): String = when (this) {
    is AppException.NetworkException -> message ?: "Koneksi bermasalah"
    is AppException.AuthException -> message ?: "Sesi berakhir"
    is AppException.DataException -> message ?: "Data tidak valid"
    is AppException.SyncException -> message ?: "Gagal sinkron"
    is AppException.UnknownError -> message ?: "Terjadi kesalahan"
}
