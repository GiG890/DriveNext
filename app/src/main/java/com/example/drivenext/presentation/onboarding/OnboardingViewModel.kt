package com.example.drivenext.presentation.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drivenext.domain.OnboardingRepository

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    private val _buttonText = MutableLiveData("Далее")
    val buttonText: LiveData<String> = _buttonText

    private val _activeIndicator = MutableLiveData(0)
    val activeIndicator: LiveData<Int> = _activeIndicator

    private val _onboardingFinished = MutableLiveData<Boolean>()
    val onboardingFinished: LiveData<Boolean> = _onboardingFinished

    fun onPageChanged(position: Int, totalPages: Int) {
        _currentPage.value = position
        _activeIndicator.value = position
        _buttonText.value = if (position == totalPages - 1) "Поехали" else "Далее"
    }

    fun onNextClicked(totalPages: Int) {
        val current = _currentPage.value ?: 0
        if (current < totalPages - 1) {
            _currentPage.value = current + 1
        } else {
            finishOnboarding()
        }
    }

    fun onSkipClicked() {
        finishOnboarding()
    }

    private fun finishOnboarding() {
        onboardingRepository.setOnboardingCompleted(true)
        _onboardingFinished.value = true
    }
}