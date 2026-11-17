package com.example.drivenext.presentation.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.drivenext.R
import com.example.drivenext.domain.UserProfile
import com.example.drivenext.presentation.common.AppViewModelFactory
import com.example.drivenext.presentation.common.LoadingDialog
import com.example.drivenext.presentation.common.ProfileFormat
import com.example.drivenext.presentation.common.openLoginAndClearStack
import com.example.drivenext.presentation.common.showErrorDialog
import com.example.drivenext.presentation.common.showInfoDialog
import com.example.drivenext.presentation.main.MainNavigator

/** «Профиль»: аватар (можно сменить), данные аккаунта и выход. */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels { AppViewModelFactory(requireContext()) }

    private var loadingDialog: LoadingDialog? = null

    // Системный выбор фото: разрешение на доступ к галерее не нужно, приложение получает Uri выбранной картинки
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.onAvatarPicked(uri.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigator = requireActivity() as MainNavigator
        loadingDialog = LoadingDialog(requireActivity())

        view.findViewById<TextView>(R.id.tvTitle).setText(R.string.profile_title)
        view.findViewById<View>(R.id.btnBack).setOnClickListener { navigator.goBack() }

        view.findViewById<View>(R.id.flAvatar).setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        view.findViewById<View>(R.id.btnLogout).setOnClickListener { viewModel.onLogoutClicked() }

        // Строки, которые не зависят от загруженного профиля
        setRow(view, R.id.rowEmail, R.string.profile_email, "")
        setRow(
            view, R.id.rowPassword, R.string.profile_password,
            getString(R.string.profile_change_password), highlighted = true
        ) { requireActivity().showInfoDialog(R.string.coming_soon) }
        setRow(view, R.id.rowGender, R.string.profile_gender, "")
        setRow(view, R.id.rowGoogle, R.string.profile_google, getString(R.string.profile_google_none))

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) bindProfile(view, profile)
        }
        viewModel.loading.observe(viewLifecycleOwner) { loadingDialog?.setVisible(it) }
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (val content = event.getContentIfNotHandled()) {
                is ProfileEvent.Failure -> requireActivity().showErrorDialog(content.error)
                is ProfileEvent.SessionExpired, is ProfileEvent.LoggedOut ->
                    requireActivity().openLoginAndClearStack()
                null -> Unit
            }
        }
    }

    override fun onDestroyView() {
        loadingDialog?.dismiss()
        loadingDialog = null
        super.onDestroyView()
    }

    private fun bindProfile(root: View, profile: UserProfile) {
        root.findViewById<TextView>(R.id.tvName).text = profile.displayName.ifBlank { profile.email }

        val joined = ProfileFormat.joinedText(requireContext(), profile.createdAt)
        val tvJoined = root.findViewById<TextView>(R.id.tvJoined)
        tvJoined.text = joined.orEmpty()
        tvJoined.visibility = if (joined == null) View.GONE else View.VISIBLE

        root.findViewById<ImageView>(R.id.ivAvatar).load(profile.avatarUrl) {
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            fallback(R.drawable.ic_person)
        }

        setValue(root, R.id.rowEmail, profile.email)
        setValue(root, R.id.rowGender, ProfileFormat.genderText(requireContext(), profile.gender))
    }

    private fun setRow(
        root: View,
        rowId: Int,
        @StringRes titleRes: Int,
        value: String,
        highlighted: Boolean = false,
        onClick: (() -> Unit)? = null
    ) {
        val row = root.findViewById<View>(rowId)
        row.findViewById<TextView>(R.id.tvRowTitle).setText(titleRes)

        val tvValue = row.findViewById<TextView>(R.id.tvRowValue)
        tvValue.text = value
        if (highlighted) {
            tvValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_purple))
        }
        if (onClick != null) {
            row.findViewById<View>(R.id.rowContent).setOnClickListener { onClick() }
        }
    }

    private fun setValue(root: View, rowId: Int, value: String) {
        root.findViewById<View>(rowId).findViewById<TextView>(R.id.tvRowValue).text = value
    }
}
