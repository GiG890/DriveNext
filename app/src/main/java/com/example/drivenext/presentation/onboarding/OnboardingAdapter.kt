package com.example.drivenext.presentation.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.drivenext.presentation.onboarding.OnboardingFragment2
import com.example.drivenext.presentation.onboarding.OnboardingFragment3

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3 // 3 слайда

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment1()
            1 -> OnboardingFragment2()
            else -> OnboardingFragment3()
        }
    }
}