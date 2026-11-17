package com.example.drivenext.presentation.common

import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.domain.DateRules
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Диалог выбора даты в стиле Material. Даты в будущем недоступны (и рождения, и выдачи прав).
 *
 * MaterialDatePicker работает с миллисекундами в UTC, поэтому переводим туда и обратно
 * через ZoneOffset.UTC: иначе при часовом поясе уехала бы дата на сутки.
 */
fun AppCompatActivity.showPastDatePicker(
    title: String,
    currentText: String,
    fallback: LocalDate,
    tag: String,
    onPicked: (String) -> Unit
) {
    if (supportFragmentManager.findFragmentByTag(tag) != null) return // защита от двойного нажатия

    val initial = DateRules.parse(currentText) ?: fallback
    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(title)
        .setSelection(initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        .setCalendarConstraints(constraints)
        .build()

    picker.addOnPositiveButtonClickListener { utcMillis ->
        val date = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        onPicked(DateRules.format(date))
    }
    picker.show(supportFragmentManager, tag)
}
