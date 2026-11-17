package com.example.drivenext.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.Event
import com.example.drivenext.presentation.common.UiError
import com.example.drivenext.presentation.common.toUiError
import kotlinx.coroutines.launch

sealed class LoginEvent {
    object InvalidEmail : LoginEvent()
    object LoggedIn : LoginEvent()
    class Failure(val error: UiError) : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginEnabled = MutableLiveData(false)
    val loginEnabled: LiveData<Boolean> = _loginEnabled

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _events = MutableLiveData<Event<LoginEvent>>()
    val events: LiveData<Event<LoginEvent>> = _events

    /** «Войти» активна, только когда заполнены оба поля. */
    fun onFieldsChanged(email: String, password: String) {
        _loginEnabled.value = email.isNotBlank() && password.isNotEmpty()
    }

    fun onLoginClicked(email: String, password: String) {
        val trimmed = email.trim()
        if (!Validators.isValidEmail(trimmed)) {
            _events.value = Event(LoginEvent.InvalidEmail)
            return
        }
        launchAuth { authRepository.login(trimmed, password) }
    }

    fun onGoogleClicked() {
        launchAuth { authRepository.loginWithGoogle() }
    }

    private fun launchAuth(request: suspend () -> Result<Unit>) {
        if (_loading.value == true) return // защита от двойного нажатия
        _loading.value = true
        viewModelScope.launch {
            val result = request()
            _loading.value = false
            val event: LoginEvent = result.fold(
                onSuccess = { LoginEvent.LoggedIn },
                onFailure = { LoginEvent.Failure(it.toUiError()) }
            )
            _events.value = Event(event)
        }
    }
}
