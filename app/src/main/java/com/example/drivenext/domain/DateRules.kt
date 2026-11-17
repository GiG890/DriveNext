package com.example.drivenext.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/**
 * Единое место для формата дат на экранах регистрации.
 * Если преподаватель захочет MM/DD/YYYY, поменяйте только PATTERN здесь.
 */
object DateRules {

    /** Шаблон java.time. uuuu нужен для STRICT-режима. */
    private const val PATTERN = "dd/MM/uuuu"
    const val DISPLAY_PATTERN = "DD/MM/YYYY"
    private const val MIN_YEAR = 1900

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(PATTERN, Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)

    fun format(date: LocalDate): String = formatter.format(date)

    fun parse(text: String): LocalDate? = try {
        LocalDate.parse(text.trim(), formatter)
    } catch (e: DateTimeParseException) {
        null
    }

    /** Дата существует, не раньше 1900 года и не в будущем. */
    fun isValidPastDate(text: String, today: LocalDate = LocalDate.now()): Boolean {
        val date = parse(text) ?: return false
        return date.year >= MIN_YEAR && !date.isAfter(today)
    }
}
