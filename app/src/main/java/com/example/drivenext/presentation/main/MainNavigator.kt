package com.example.drivenext.presentation.main

import androidx.annotation.StringRes

/** Навигация между экранами внутри MainActivity. Фрагменты общаются с Activity только через этот интерфейс. */
interface MainNavigator {
    fun openSearchResults(query: String)
    fun openProfile()

    /** Экран-заглушка для разделов, которые будут сверстаны в следующих работах. */
    fun openPlaceholder(@StringRes titleRes: Int)

    fun goBack()
}
