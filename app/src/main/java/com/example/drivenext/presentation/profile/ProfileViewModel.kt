package com.example.drivenext.presentation.profile

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

sealed class ProfileEvent {
    class Failure(val error: UiError) : ProfileEvent()
    object SessionExpired : ProfileEvent()
    object LoggedOut : ProfileEvent()
}

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _events = MutableLiveData<Event<ProfileEvent>>()
    val events: LiveData<Event<ProfileEvent>> = _events

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            authRepository.getProfile().fold(
                onSuccess = { _profile.value = it },
                onFailure = { emitFailure(it) }
            )
            _loading.value = false
        }
    }

    /** Новый аватар: загружаем на сервер, затем перечитываем профиль, чтобы показать актуальную ссылку. */
    fun onAvatarPicked(imageUri: String) {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            val update = authRepository.updateAvatar(imageUri)
            if (update.isSuccess) {
                authRepository.getProfile().fold(
                    onSuccess = { _profile.value = it },
                    onFailure = { emitFailure(it) }
                )
            } else {
                emitFailure(update.exceptionOrNull() ?: IllegalStateException("avatar update failed"))
            }
            _loading.value = false
        }
    }

    /** Выход из аккаунта: стираем сессию и сообщаем экрану, что нужно открыть вход. */
    fun onLogoutClicked() {
        authRepository.logout()
        _events.value = Event(ProfileEvent.LoggedOut)
    }

    private fun emitFailure(error: Throwable) {
        val event: ProfileEvent =
            if ((error as? AppException)?.error == AppError.SESSION_EXPIRED) {
                authRepository.logout()
                ProfileEvent.SessionExpired
            } else {
                ProfileEvent.Failure(error.toUiError())
            }
        _events.value = Event(event)
    }
}
