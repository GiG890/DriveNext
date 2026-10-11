package com.example.drivenext.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.drivenext.R
import com.example.drivenext.data.repository.OnboardingRepositoryImpl
import com.example.drivenext.presentation.login.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    private val viewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory(
            onboardingRepository = OnboardingRepositoryImpl(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)

        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // Слушаем смену страниц -> передаем во ViewModel
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onPageChanged(position, adapter.itemCount)
            }
        })

        // Кнопка "Далее"
        btnNext.setOnClickListener {
            viewModel.onNextClicked(adapter.itemCount)
        }

        // Кнопка "Пропустить"
        tvSkip.setOnClickListener {
            viewModel.onSkipClicked()
        }

        // Подписываемся на ViewModel
        viewModel.currentPage.observe(this) { page ->
            if (viewPager.currentItem != page) {
                viewPager.currentItem = page
            }
        }

        viewModel.buttonText.observe(this) { text ->
            btnNext.text = text
        }

        viewModel.activeIndicator.observe(this) { active ->
            updateIndicator(active)
        }

        viewModel.onboardingFinished.observe(this) { finished ->
            if (finished) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun updateIndicator(position: Int) {
        val activeWidth = (40 * resources.displayMetrics.density).toInt()
        val inactiveWidth = (16 * resources.displayMetrics.density).toInt()

        val params1 = indicator1.layoutParams
        val params2 = indicator2.layoutParams
        val params3 = indicator3.layoutParams

        params1.width = if (position == 0) activeWidth else inactiveWidth
        params2.width = if (position == 1) activeWidth else inactiveWidth
        params3.width = if (position == 2) activeWidth else inactiveWidth

        indicator1.layoutParams = params1
        indicator2.layoutParams = params2
        indicator3.layoutParams = params3

        indicator1.setBackgroundResource(if (position == 0) R.drawable.indicator_active else R.drawable.indicator_inactive)
        indicator2.setBackgroundResource(if (position == 1) R.drawable.indicator_active else R.drawable.indicator_inactive)
        indicator3.setBackgroundResource(if (position == 2) R.drawable.indicator_active else R.drawable.indicator_inactive)
    }
}