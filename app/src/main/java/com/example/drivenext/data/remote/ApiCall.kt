package com.example.drivenext.data.remote

import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Выполняет блокирующий сетевой код на фоновом потоке и превращает любое исключение в Result.failure.
 * Приложение при сбоях не падает: ViewModel получает ошибку и показывает диалог.
 */
suspend fun <T> apiCall(block: () -> T): Result<T> = withContext(Dispatchers.IO) {
    try {
        Result.success(block())
    } catch (e: AppException) {
        Result.failure(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(AppException(AppError.UNKNOWN, e.message, e))
    }
}
