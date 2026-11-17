package com.example.drivenext

import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.home.HomeFragment
import com.example.drivenext.presentation.main.MainNavigator
import com.example.drivenext.presentation.placeholder.PlaceholderFragment
import com.example.drivenext.presentation.profile.ProfileFragment
import com.example.drivenext.presentation.search.SearchResultsFragment
import com.example.drivenext.presentation.settings.SettingsFragment

/**
 * Главный экран приложения: контейнер для фрагментов и нижнее меню из трёх вкладок
 * (главная, бронирования, настройки).
 */
class MainActivity : AppCompatActivity(), MainNavigator {

    private enum class Tab { HOME, BOOKINGS, SETTINGS }

    private lateinit var tabHome: ImageView
    private lateinit var tabBookings: ImageView
    private lateinit var tabSettings: ImageView

    private var currentTab = Tab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_main)
        findViewById<android.view.View>(R.id.main).applySystemBarInsets()

        tabHome = findViewById(R.id.tabHome)
        tabBookings = findViewById(R.id.tabBookings)
        tabSettings = findViewById(R.id.tabSettings)

        tabHome.setOnClickListener { selectTab(Tab.HOME) }
        tabBookings.setOnClickListener { selectTab(Tab.BOOKINGS) }
        tabSettings.setOnClickListener { selectTab(Tab.SETTINGS) }

        if (savedInstanceState == null) {
            selectTab(Tab.HOME)
        } else {
            // Фрагменты система восстановит сама, нам нужно только подсветить вкладку
            currentTab = Tab.values()[savedInstanceState.getInt(STATE_TAB, 0)]
            updateTabColors()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_TAB, currentTab.ordinal)
    }

    /** Переключает вкладку: сбрасывает вложенные экраны и показывает корневой экран вкладки. */
    private fun selectTab(tab: Tab) {
        currentTab = tab
        updateTabColors()

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val root: Fragment = when (tab) {
            Tab.HOME -> HomeFragment()
            Tab.BOOKINGS -> PlaceholderFragment.newInstance(R.string.bookings_title, showBack = false)
            Tab.SETTINGS -> SettingsFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, root)
            .commit()
    }

    private fun updateTabColors() {
        paint(tabHome, currentTab == Tab.HOME)
        paint(tabBookings, currentTab == Tab.BOOKINGS)
        paint(tabSettings, currentTab == Tab.SETTINGS)
    }

    private fun paint(tab: ImageView, selected: Boolean) {
        val color = if (selected) R.color.brand_purple else R.color.text_hint
        ImageViewCompat.setImageTintList(
            tab,
            ContextCompat.getColorStateList(this, color)
        )
    }

    private fun push(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // --- MainNavigator ---

    override fun openSearchResults(query: String) = push(SearchResultsFragment.newInstance(query))

    override fun openProfile() = push(ProfileFragment())

    override fun openPlaceholder(@StringRes titleRes: Int) =
        push(PlaceholderFragment.newInstance(titleRes, showBack = true))

    override fun goBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    private companion object {
        const val STATE_TAB = "state_tab"
    }
}
