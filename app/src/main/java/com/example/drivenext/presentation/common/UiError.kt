package com.example.drivenext.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import com.example.drivenext.R
import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException

/** Ошибка, готовая к показу: текст из ресурсов плюс (если есть) текст, который вернул сервер. */
class UiError(@StringRes val messageRes: Int, val details: String? = null) {
    /** Нет интернета: вместо диалога показываем экран No Connection. */
    val isNoConnection: Boolean
        get() = messageRes == R.string.error_no_connection
}

fun Throwable.toUiError(): UiError {
    val exception = this as? AppException ?: return UiError(R.string.auth_error_generic, message)
    return when (exception.error) {
        AppError.NO_CONNECTION -> UiError(R.string.error_no_connection)
        AppError.INVALID_CREDENTIALS -> UiError(R.string.auth_error_invalid_credentials)
        AppError.EMAIL_TAKEN -> UiError(R.string.auth_error_email_taken)
        AppError.EMAIL_NOT_CONFIRMED -> UiError(R.string.error_email_not_confirmed)
        AppError.GOOGLE_FAILED -> UiError(R.string.auth_error_google_failed)
        AppError.SESSION_EXPIRED -> UiError(R.string.error_session_expired)
        AppError.SERVER -> UiError(R.string.error_server, exception.serverMessage)
        AppError.UNKNOWN -> UiError(R.string.auth_error_generic, exception.serverMessage)
    }
}

/**
 * Текст для экранов со списком: фиксированная фраза из ТЗ («Не удалось загрузить данные...»)
 * и ниже причина, если она известна (нет сети или ответ сервера).
 */
fun UiError.withBase(context: Context, @StringRes baseRes: Int): String {
    val base = context.getString(baseRes)
    val extra = when {
        !details.isNullOrBlank() -> details
        messageRes == R.string.error_no_connection -> context.getString(messageRes)
        else -> null
    }
    return if (extra == null) base else "$base\n$extra"
}
