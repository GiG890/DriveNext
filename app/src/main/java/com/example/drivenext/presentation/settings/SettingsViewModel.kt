package com.example.drivenext.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.UserProfile
import com.example.drivenext.presentation.common.Event
import com.example.drivenext.presentation.common.UiError
import com.example.drivenext.presentation.common.toUiError
import kotlinx.coroutines.launch

sealed class SettingsEvent {
    class Failure(val error: UiError) : SettingsEvent()
    object SessionExpired : SettingsEvent()
}

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _events = MutableLiveData<Event<SettingsEvent>>()
    val events: LiveData<Event<SettingsEvent>> = _events

    /** Загружает профиль для шапки экрана. Вызывается при каждом показе экрана (аватар мог измениться). */
    fun loadProfile() {
        viewModelScope.launch {
            authRepository.getProfile().fold(
                onSuccess = { _profile.value = it },
                onFailure = { error ->
                    val event: SettingsEvent =
                        if ((error as? AppException)?.error == AppError.SESSION_EXPIRED) {
                            authRepository.logout()
                            SettingsEvent.SessionExpired
                        } else {
                            SettingsEvent.Failure(error.toUiError())
                        }
                    _events.value = Event(event)
                }
            )
        }
    }
}
