package com.example.drivenext.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.drivenext.MainActivity
import com.example.drivenext.R
import com.example.drivenext.data.repository.AuthRepositoryImpl
import com.example.drivenext.data.repository.NetworkRepositoryImpl
import com.example.drivenext.data.repository.OnboardingRepositoryImpl
import com.example.drivenext.domain.SplashDestination
import com.example.drivenext.presentation.login.LoginActivity
import com.example.drivenext.presentation.noconnection.NoConnectionActivity
import com.example.drivenext.presentation.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(
            onboardingRepository = OnboardingRepositoryImpl(applicationContext),
            networkRepository = NetworkRepositoryImpl(applicationContext),
            authRepository = AuthRepositoryImpl(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewModel.destination.observe(this) { destination ->
            when (destination) {
                SplashDestination.ONBOARDING -> navigateTo(OnboardingActivity::class.java)
                SplashDestination.LOGIN -> navigateTo(LoginActivity::class.java)
                SplashDestination.MAIN -> navigateTo(MainActivity::class.java)
                SplashDestination.NO_CONNECTION -> navigateTo(NoConnectionActivity::class.java)
            }
        }

        viewModel.decideNextScreen()
    }

    private fun navigateTo(target: Class<*>) {
        startActivity(Intent(this, target))
        finish()
    }
}