package com.example.drivenext.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.DateRules
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.Event
import com.example.drivenext.presentation.common.UiError
import com.example.drivenext.presentation.common.toUiError
import kotlinx.coroutines.launch

sealed class Step3Event {
    class FieldErrors(val licenseInvalid: Boolean, val issueDateInvalid: Boolean) : Step3Event()
    object PhotosMissing : Step3Event()
    class Failure(val error: UiError) : Step3Event()
    object Registered : Step3Event()
}

class RegistrationStep3ViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _nextEnabled = MutableLiveData(false)
    val nextEnabled: LiveData<Boolean> = _nextEnabled

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _events = MutableLiveData<Event<Step3Event>>()
    val events: LiveData<Event<Step3Event>> = _events

    fun onFieldsChanged(licenseNumber: String, issueDate: String) {
        _nextEnabled.value = licenseNumber.isNotBlank() && issueDate.isNotBlank()
    }

    fun onNextClicked(
        base: RegistrationData,
        licenseNumber: String,
        issueDate: String,
        avatarUri: String?,
        licensePhotoUri: String?,
        passportPhotoUri: String?
    ) {
        if (_loading.value == true) return // защита от двойного нажатия

        val license = licenseNumber.trim()
        val issue = issueDate.trim()

        val licenseInvalid = !Validators.isValidLicenseNumber(license)
        val issueInvalid = !DateRules.isValidPastDate(issue)
        if (licenseInvalid || issueInvalid) {
            _events.value = Event(Step3Event.FieldErrors(licenseInvalid, issueInvalid))
            return
        }

        if (licensePhotoUri == null || passportPhotoUri == null) {
            _events.value = Event(Step3Event.PhotosMissing)
            return
        }

        val data = base.copy(
            licenseNumber = license,
            licenseIssueDate = issue,
            avatarUri = avatarUri,
            licensePhotoUri = licensePhotoUri,
            passportPhotoUri = passportPhotoUri
        )

        _loading.value = true
        viewModelScope.launch {
            val result = authRepository.register(data)
            _loading.value = false
            val event: Step3Event = result.fold(
                onSuccess = { Step3Event.Registered },
                onFailure = { Step3Event.Failure(it.toUiError()) }
            )
            _events.value = Event(event)
        }
    }
}
