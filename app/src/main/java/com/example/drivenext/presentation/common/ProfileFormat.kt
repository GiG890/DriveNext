package com.example.drivenext.presentation.common

import android.content.Context
import com.example.drivenext.R
import com.example.drivenext.domain.Gender
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Подготовка данных профиля к показу на экранах «Настройки» и «Профиль». */
object ProfileFormat {

    // Месяцы в предложном падеже: «Присоединился в июле 2024»
    private val MONTHS = arrayOf(
        "январе", "феврале", "марте", "апреле", "мае", "июне",
        "июле", "августе", "сентябре", "октябре", "ноябре", "декабре"
    )

    /** createdAt: дата в формате ISO-8601 от сервера, например 2026-11-15T10:20:30+00:00. */
    fun joinedText(context: Context, createdAt: String?): String? {
        val date = parseDate(createdAt) ?: return null
        return context.getString(R.string.profile_joined, MONTHS[date.monthValue - 1], date.year)
    }

    fun genderText(context: Context, gender: Gender?): String = when (gender) {
        Gender.MALE -> context.getString(R.string.auth_gender_male)
        Gender.FEMALE -> context.getString(R.string.auth_gender_female)
        null -> context.getString(R.string.profile_no_value)
    }

    private fun parseDate(iso: String?): LocalDate? {
        if (iso == null || iso.length < 10) return null
        return try {
            LocalDate.parse(iso.substring(0, 10))
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
