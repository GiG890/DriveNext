package com.example.drivenext.presentation.register

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.data.repository.AuthRepositoryImpl
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.DateRules
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.setImageFromUri
import com.example.drivenext.presentation.common.showError
import com.example.drivenext.presentation.common.showMessage
import com.example.drivenext.presentation.common.showPastDatePicker
import com.example.drivenext.presentation.common.toMessageRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.time.LocalDate

/** Регистрация, шаг 3: фото профиля, водительское удостоверение, фото документов. */
class RegistrationStep3Activity : AppCompatActivity() {

    private enum class PhotoTarget { AVATAR, LICENSE, PASSPORT }

    private lateinit var baseData: RegistrationData
    private lateinit var authRepository: AuthRepository

    private lateinit var tilLicenseNumber: TextInputLayout
    private lateinit var tilIssueDate: TextInputLayout
    private lateinit var etLicenseNumber: TextInputEditText
    private lateinit var etIssueDate: TextInputEditText
    private lateinit var ivAvatar: ImageView
    private lateinit var ivLicensePhoto: ImageView
    private lateinit var ivPassportPhoto: ImageView
    private lateinit var tvLicensePhotoStatus: TextView
    private lateinit var tvPassportPhotoStatus: TextView
    private lateinit var btnNext: Button

    private var avatarUri: Uri? = null
    private var licenseUri: Uri? = null
    private var passportUri: Uri? = null

    // Какое фото сейчас выбираем и куда камера должна записать снимок
    private var pendingTarget: PhotoTarget? = null
    private var pendingCameraUri: Uri? = null

    // Activity Result API: контракты регистрируются до STARTED, поэтому прямо в свойствах.
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val target = pendingTarget
        if (uri != null && target != null && !applyPhoto(target, uri)) {
            showMessage(R.string.auth_error_photo_failed)
        }
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val target = pendingTarget
        val uri = pendingCameraUri
        if (success && target != null && uri != null && !applyPhoto(target, uri)) {
            showMessage(R.string.auth_error_photo_failed)
        }
        pendingCameraUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = IntentCompat.getSerializableExtra(intent, EXTRA_DATA, RegistrationData::class.java)
        if (data == null) {
            finish()
            return
        }
        baseData = data

        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_registration_step3)
        findViewById<View>(R.id.root).applySystemBarInsets()

        authRepository = AuthRepositoryImpl(applicationContext)

        tilLicenseNumber = findViewById(R.id.tilLicenseNumber)
        tilIssueDate = findViewById(R.id.tilIssueDate)
        etLicenseNumber = findViewById(R.id.etLicenseNumber)
        etIssueDate = findViewById(R.id.etIssueDate)
        ivAvatar = findViewById(R.id.ivAvatar)
        ivLicensePhoto = findViewById(R.id.ivLicensePhoto)
        ivPassportPhoto = findViewById(R.id.ivPassportPhoto)
        tvLicensePhotoStatus = findViewById(R.id.tvLicensePhotoStatus)
        tvPassportPhotoStatus = findViewById(R.id.tvPassportPhotoStatus)
        btnNext = findViewById(R.id.btnNext)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        etLicenseNumber.doAfterTextChanged {
            tilLicenseNumber.showError(null)
            updateNextButton()
        }
        etIssueDate.doAfterTextChanged {
            tilIssueDate.showError(null)
            updateNextButton()
        }

        tilIssueDate.setStartIconOnClickListener { openIssueDatePicker() }
        etIssueDate.setOnClickListener { openIssueDatePicker() }

        findViewById<View>(R.id.flAvatar).setOnClickListener { showPhotoSourceDialog(PhotoTarget.AVATAR) }
        findViewById<View>(R.id.rowLicensePhoto).setOnClickListener { showPhotoSourceDialog(PhotoTarget.LICENSE) }
        findViewById<View>(R.id.rowPassportPhoto).setOnClickListener { showPhotoSourceDialog(PhotoTarget.PASSPORT) }

        btnNext.setOnClickListener { onNextClicked() }

        restoreState(savedInstanceState)
        updateNextButton()
    }

    // Пока открыта камера или галерея, система может выгрузить процесс: сохраняем выбор.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_TARGET, pendingTarget?.name)
        outState.putString(STATE_CAMERA_URI, pendingCameraUri?.toString())
        outState.putString(STATE_AVATAR, avatarUri?.toString())
        outState.putString(STATE_LICENSE, licenseUri?.toString())
        outState.putString(STATE_PASSPORT, passportUri?.toString())
    }

    private fun restoreState(state: Bundle?) {
        state ?: return
        pendingTarget = state.getString(STATE_TARGET)?.let { PhotoTarget.valueOf(it) }
        pendingCameraUri = state.getString(STATE_CAMERA_URI)?.let { Uri.parse(it) }
        state.getString(STATE_AVATAR)?.let { applyPhoto(PhotoTarget.AVATAR, Uri.parse(it)) }
        state.getString(STATE_LICENSE)?.let { applyPhoto(PhotoTarget.LICENSE, Uri.parse(it)) }
        state.getString(STATE_PASSPORT)?.let { applyPhoto(PhotoTarget.PASSPORT, Uri.parse(it)) }
    }

    private fun openIssueDatePicker() {
        showPastDatePicker(
            title = getString(R.string.auth_label_issue_date),
            currentText = etIssueDate.text?.toString().orEmpty(),
            fallback = LocalDate.now(),
            tag = "issue_date_picker"
        ) { etIssueDate.setText(it) }
    }

    private fun showPhotoSourceDialog(target: PhotoTarget) {
        val options = arrayOf(
            getString(R.string.auth_photo_gallery),
            getString(R.string.auth_photo_camera)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.auth_photo_source_title)
            .setItems(options) { _, which ->
                pendingTarget = target
                if (which == 0) openGallery() else openCamera()
            }
            .show()
    }

    private fun openGallery() {
        pickImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun openCamera() {
        // Камере нужен Uri, куда писать файл: берём его через FileProvider (см. манифест и file_paths.xml)
        val dir = File(cacheDir, "photos").apply { mkdirs() }
        val file = File.createTempFile("photo_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        pendingCameraUri = uri
        try {
            takePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingCameraUri = null
            showMessage(R.string.auth_error_camera_unavailable)
        }
    }

    /** Показывает фото в нужной ячейке и запоминает Uri. Возвращает false, если файл не открылся. */
    private fun applyPhoto(target: PhotoTarget, uri: Uri): Boolean {
        val imageView = when (target) {
            PhotoTarget.AVATAR -> ivAvatar
            PhotoTarget.LICENSE -> ivLicensePhoto
            PhotoTarget.PASSPORT -> ivPassportPhoto
        }
        if (!imageView.setImageFromUri(uri)) return false

        imageView.setPadding(0, 0, 0, 0)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        when (target) {
            PhotoTarget.AVATAR -> avatarUri = uri
            PhotoTarget.LICENSE -> {
                licenseUri = uri
                tvLicensePhotoStatus.setText(R.string.auth_photo_uploaded)
            }
            PhotoTarget.PASSPORT -> {
                passportUri = uri
                tvPassportPhotoStatus.setText(R.string.auth_photo_uploaded)
            }
        }
        return true
    }

    private fun updateNextButton() {
        btnNext.isEnabled = !etLicenseNumber.text.isNullOrBlank() && !etIssueDate.text.isNullOrBlank()
    }

    private fun onNextClicked() {
        val licenseNumber = etLicenseNumber.text?.toString().orEmpty().trim()
        val issueDate = etIssueDate.text?.toString().orEmpty().trim()

        var valid = true
        if (!Validators.isValidLicenseNumber(licenseNumber)) {
            tilLicenseNumber.showError(getString(R.string.auth_error_license_length))
            valid = false
        }
        if (!DateRules.isValidPastDate(issueDate)) {
            tilIssueDate.showError(getString(R.string.auth_error_issue_date))
            valid = false
        }
        if (!valid) return

        if (licenseUri == null || passportUri == null) {
            showMessage(R.string.auth_error_photos_required)
            return
        }

        val data = baseData.copy(
            licenseNumber = licenseNumber,
            licenseIssueDate = issueDate,
            avatarUri = avatarUri?.toString(),
            licensePhotoUri = licenseUri?.toString(),
            passportPhotoUri = passportUri?.toString()
        )

        authRepository.register(data).fold(
            onSuccess = { startActivity(Intent(this, RegistrationSuccessActivity::class.java)) },
            onFailure = { showMessage(it.toMessageRes()) }
        )
    }

    companion object {
        private const val EXTRA_DATA = "registration_data"
        private const val STATE_TARGET = "state_target"
        private const val STATE_CAMERA_URI = "state_camera_uri"
        private const val STATE_AVATAR = "state_avatar"
        private const val STATE_LICENSE = "state_license"
        private const val STATE_PASSPORT = "state_passport"

        fun newIntent(context: Context, data: RegistrationData): Intent =
            Intent(context, RegistrationStep3Activity::class.java).putExtra(EXTRA_DATA, data)
    }
}
