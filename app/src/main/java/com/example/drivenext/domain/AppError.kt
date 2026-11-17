package com.example.drivenext.domain

/** Типы ошибок, которые слой данных передаёт наверх. Тексты для пользователя подбирает слой представления. */
enum class AppError {
    NO_CONNECTION,
    INVALID_CREDENTIALS,
    EMAIL_TAKEN,
    EMAIL_NOT_CONFIRMED,
    GOOGLE_FAILED,
    SESSION_EXPIRED,
    SERVER,
    UNKNOWN
}

/** serverMessage: текст ошибки от сервера (если он был), его показываем в диалоге. */
class AppException(
    val error: AppError,
    val serverMessage: String? = null,
    cause: Throwable? = null
) : Exception(serverMessage ?: error.name, cause)
