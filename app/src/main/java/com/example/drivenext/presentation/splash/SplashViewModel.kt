package com.example.drivenext.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.NetworkRepository
import com.example.drivenext.domain.OnboardingRepository
import com.example.drivenext.domain.SplashDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val networkRepository: NetworkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = androidx.lifecycle.MutableLiveData<SplashDestination>()
    val destination: androidx.lifecycle.LiveData<SplashDestination> = _destination

    fun decideNextScreen() {
        viewModelScope.launch {

            delay(2500)

            if (!networkRepository.isNetworkAvailable()) {
                _destination.value = SplashDestination.NO_CONNECTION
                return@launch
            }

            if (!onboardingRepository.isOnboardingCompleted()) {
                _destination.value = SplashDestination.ONBOARDING
                return@launch
            }

            if (!authRepository.isLoggedIn()) {
                _destination.value = SplashDestination.LOGIN
                return@launch
            }

            _destination.value = SplashDestination.MAIN
        }
    }
}