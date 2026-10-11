package com.example.drivenext.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.NetworkRepository
import com.example.drivenext.domain.OnboardingRepository

class SplashViewModelFactory(
    private val onboardingRepository: OnboardingRepository,
    private val networkRepository: NetworkRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(onboardingRepository, networkRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}