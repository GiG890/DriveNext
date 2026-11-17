package com.example.drivenext.presentation.register

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drivenext.R
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.Event

class Step1Errors(
    @StringRes val emailRes: Int?,
    @StringRes val passwordRes: Int?,
    @StringRes val repeatRes: Int?,
    val consentMissing: Boolean
) {
    val hasAny: Boolean
        get() = emailRes != null || passwordRes != null || repeatRes != null || consentMissing
}

sealed class Step1Event {
    class ShowErrors(val errors: Step1Errors) : Step1Event()
    class Proceed(val data: RegistrationData) : Step1Event()
}

/**
 * Занятость почты теперь проверяет сервер при создании аккаунта (шаг 3),
 * поэтому на этом шаге репозиторий не нужен: только проверка полей.
 */
class RegistrationStep1ViewModel : ViewModel() {

    private val _events = MutableLiveData<Event<Step1Event>>()
    val events: LiveData<Event<Step1Event>> = _events

    fun onNextClicked(email: String, password: String, repeatPassword: String, consentChecked: Boolean) {
        val trimmedEmail = email.trim()

        val emailRes: Int? =
            if (!Validators.isValidEmail(trimmedEmail)) R.string.auth_error_email else null
        val passwordRes: Int? =
            if (password.isEmpty()) R.string.auth_error_password_required else null
        val repeatRes: Int? =
            if (repeatPassword != password) R.string.auth_error_passwords_mismatch else null

        val errors = Step1Errors(emailRes, passwordRes, repeatRes, !consentChecked)

        val event: Step1Event = if (errors.hasAny) {
            Step1Event.ShowErrors(errors)
        } else {
            Step1Event.Proceed(RegistrationData(email = trimmedEmail, password = password))
        }
        _events.value = Event(event)
    }
}
