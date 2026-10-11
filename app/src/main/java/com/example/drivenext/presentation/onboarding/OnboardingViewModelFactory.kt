package com.example.drivenext.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drivenext.domain.OnboardingRepository

class OnboardingViewModelFactory(
    private val onboardingRepository: OnboardingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(onboardingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}