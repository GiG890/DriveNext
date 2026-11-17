package com.example.drivenext.presentation.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drivenext.data.remote.SessionManager
import com.example.drivenext.data.remote.SupabaseHttp
import com.example.drivenext.data.repository.AuthRepositoryImpl
import com.example.drivenext.data.repository.CarRepositoryImpl
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.CarRepository
import com.example.drivenext.presentation.home.HomeViewModel
import com.example.drivenext.presentation.login.LoginViewModel
import com.example.drivenext.presentation.profile.ProfileViewModel
import com.example.drivenext.presentation.register.RegistrationStep3ViewModel
import com.example.drivenext.presentation.search.SearchResultsViewModel
import com.example.drivenext.presentation.settings.SettingsViewModel

/**
 * Одна фабрика на все ViewModel, которым нужны репозитории.
 * Репозитории создаются один раз на фабрику и общаются с сервером через общий HTTP-клиент.
 */
class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext
    private val http by lazy { SupabaseHttp() }
    private val session by lazy { SessionManager(appContext, http) }
    private val authRepository: AuthRepository by lazy { AuthRepositoryImpl(appContext, http, session) }
    private val carRepository: CarRepository by lazy { CarRepositoryImpl(session, http) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(authRepository) as T
            modelClass.isAssignableFrom(RegistrationStep3ViewModel::class.java) ->
                RegistrationStep3ViewModel(authRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(carRepository) as T
            modelClass.isAssignableFrom(SearchResultsViewModel::class.java) ->
                SearchResultsViewModel(carRepository) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(authRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(authRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
