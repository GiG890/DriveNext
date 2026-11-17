package com.example.drivenext.presentation.common

/**
 * Одноразовое событие для LiveData (показать диалог, перейти на экран).
 * После поворота экрана LiveData отдаёт значение заново, а Event второй раз содержимое не выдаст,
 * поэтому диалог не появится повторно.
 */
class Event<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? {
        if (handled) return null
        handled = true
        return content
    }
}
