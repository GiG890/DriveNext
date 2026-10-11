package com.example.drivenext.data.repository

import android.content.Context
import com.example.drivenext.domain.OnboardingRepository

class OnboardingRepositoryImpl(context: Context) : OnboardingRepository {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_done", false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_done", completed).apply()
    }
}