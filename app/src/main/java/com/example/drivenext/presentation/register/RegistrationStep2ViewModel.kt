package com.example.drivenext.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drivenext.domain.DateRules
import com.example.drivenext.domain.Gender
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.presentation.common.Event

sealed class Step2Event {
    object RequiredMissing : Step2Event()
    object InvalidBirthDate : Step2Event()
    class Proceed(val data: RegistrationData) : Step2Event()
}

/** Репозиторий на этом шаге не нужен: данные только проверяются и передаются дальше. */
class RegistrationStep2ViewModel : ViewModel() {

    private val _nextEnabled = MutableLiveData(false)
    val nextEnabled: LiveData<Boolean> = _nextEnabled

    private val _events = MutableLiveData<Event<Step2Event>>()
    val events: LiveData<Event<Step2Event>> = _events

    fun onFieldsChanged(lastName: String, firstName: String, birthDate: String, gender: Gender?) {
        _nextEnabled.value = requiredFilled(lastName, firstName, birthDate, gender)
    }

    fun onNextClicked(
        base: RegistrationData,
        lastName: String,
        firstName: String,
        patronymic: String,
        birthDate: String,
        gender: Gender?
    ) {
        val event: Step2Event = when {
            !requiredFilled(lastName, firstName, birthDate, gender) -> Step2Event.RequiredMissing
            !DateRules.isValidPastDate(birthDate.trim()) -> Step2Event.InvalidBirthDate
            else -> Step2Event.Proceed(
                base.copy(
                    lastName = lastName.trim(),
                    firstName = firstName.trim(),
                    patronymic = patronymic.trim(),
                    birthDate = birthDate.trim(),
                    gender = gender
                )
            )
        }
        _events.value = Event(event)
    }

    private fun requiredFilled(
        lastName: String,
        firstName: String,
        birthDate: String,
        gender: Gender?
    ): Boolean = lastName.isNotBlank() && firstName.isNotBlank() &&
        birthDate.isNotBlank() && gender != null
}
