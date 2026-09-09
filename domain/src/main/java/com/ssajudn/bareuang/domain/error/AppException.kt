package com.ssajudn.bareuang.domain.error

/**
 * Domain-level typed errors — tidak bocor HttpException/IOException ke ui.
 * Clean Architecture: error type milik domain, data map ke sini via ApiErrorParser.
 */
sealed class AppException(
    override val message: String?,
    override val cause: Throwable? = null,
    open val code: AppErrorCode = AppErrorCode.UNKNOWN,
) : Exception(message, cause) {

    class NetworkException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.NETWORK)

    class AuthException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.AUTH)

    class DataException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.DATA)

    /**
     * Distinct marker for the daily-budget soft nudge: lets callers distinguish
     * "exceeds today's allowance" from generic data failures so they can offer a
     * "save anyway" confirmation instead of a hard error.
     */
    class DailyBudgetExceededException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.DAILY_BUDGET_EXCEEDED)

    class SyncException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.SYNC)

    class UnknownError(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.UNKNOWN)
}

enum class AppErrorCode {
    NETWORK,
    AUTH,
    DATA,
    DAILY_BUDGET_EXCEEDED,
    SYNC,
    UNKNOWN,
}
