package com.example.drivenext.presentation.common

/** Состояние загрузки данных для экрана: крутится индикатор, данные получены или случилась ошибка. */
sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    class Success<T>(val data: T) : LoadState<T>()
    class Error(val error: UiError) : LoadState<Nothing>()
}
