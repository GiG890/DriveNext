package com.example.drivenext.presentation.settings

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.drivenext.R
import com.example.drivenext.domain.UserProfile
import com.example.drivenext.presentation.common.AppViewModelFactory
import com.example.drivenext.presentation.common.openLoginAndClearStack
import com.example.drivenext.presentation.common.showErrorDialog
import com.example.drivenext.presentation.common.showInfoDialog
import com.example.drivenext.presentation.main.MainNavigator

/** «Настройки»: профиль пользователя сверху и меню разделов. */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels { AppViewModelFactory(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigator = requireActivity() as MainNavigator

        view.findViewById<View>(R.id.rowProfile).setOnClickListener { navigator.openProfile() }

        setupRow(view, R.id.rowBookings, null, R.string.settings_bookings) {
            navigator.openPlaceholder(R.string.bookings_title)
        }
        setupRow(view, R.id.rowTheme, R.drawable.ic_row_sun, R.string.settings_theme) { showComingSoon() }
        setupRow(view, R.id.rowNotifications, R.drawable.ic_row_bell, R.string.settings_notifications) {
            showComingSoon()
        }
        setupRow(view, R.id.rowAddCar, null, R.string.settings_add_car) {
            navigator.openPlaceholder(R.string.add_car_title)
        }
        setupRow(view, R.id.rowHelp, R.drawable.ic_row_help, R.string.settings_help) { showComingSoon() }
        setupRow(view, R.id.rowInvite, R.drawable.ic_row_mail, R.string.settings_invite) { showComingSoon() }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) bindProfile(view, profile)
        }
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (val content = event.getContentIfNotHandled()) {
                is SettingsEvent.Failure -> requireActivity().showErrorDialog(content.error)
                is SettingsEvent.SessionExpired -> requireActivity().openLoginAndClearStack()
                null -> Unit
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadProfile()
    }

    private fun bindProfile(root: View, profile: UserProfile) {
        root.findViewById<TextView>(R.id.tvName).text = profile.displayName.ifBlank { profile.email }
        root.findViewById<TextView>(R.id.tvEmail).text = profile.email

        // Если аватара нет или он не загрузился, остаётся иконка человека
        root.findViewById<ImageView>(R.id.ivAvatar).load(profile.avatarUrl) {
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            fallback(R.drawable.ic_person)
        }
    }

    private fun setupRow(
        root: View,
        rowId: Int,
        @DrawableRes iconRes: Int?,
        @StringRes titleRes: Int,
        onClick: () -> Unit
    ) {
        val row = root.findViewById<View>(rowId)
        row.findViewById<TextView>(R.id.tvRowTitle).setText(titleRes)

        val icon = row.findViewById<ImageView>(R.id.ivRowIcon)
        if (iconRes != null) {
            icon.setImageResource(iconRes)
        } else {
            // Место под иконку остаётся, чтобы заголовки стояли ровно, как в макете
            icon.visibility = View.INVISIBLE
        }
        row.setOnClickListener { onClick() }
    }

    private fun showComingSoon() {
        requireActivity().showInfoDialog(R.string.coming_soon)
    }
}
