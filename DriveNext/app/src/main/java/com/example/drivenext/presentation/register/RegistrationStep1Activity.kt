package com.example.drivenext.presentation.register

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.data.repository.AuthRepositoryImpl
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.showError
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Регистрация, шаг 1: почта, пароль, согласие с условиями. */
class RegistrationStep1Activity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilRepeatPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etRepeatPassword: TextInputEditText
    private lateinit var cbConsent: MaterialCheckBox
    private lateinit var tvConsentError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_registration_step1)
        findViewById<View>(R.id.root).applySystemBarInsets()

        authRepository = AuthRepositoryImpl(applicationContext)

        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilRepeatPassword = findViewById(R.id.tilRepeatPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRepeatPassword = findViewById(R.id.etRepeatPassword)
        cbConsent = findViewById(R.id.cbConsent)
        tvConsentError = findViewById(R.id.tvConsentError)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Ошибка пропадает, как только пользователь начинает исправлять поле
        etEmail.doAfterTextChanged { tilEmail.showError(null) }
        etPassword.doAfterTextChanged {
            tilPassword.showError(null)
            tilRepeatPassword.showError(null)
        }
        etRepeatPassword.doAfterTextChanged { tilRepeatPassword.showError(null) }
        cbConsent.setOnCheckedChangeListener { _, _ -> tvConsentError.visibility = View.GONE }

        findViewById<View>(R.id.btnNext).setOnClickListener { onNextClicked() }
    }

    private fun onNextClicked() {
        val email = etEmail.text?.toString().orEmpty().trim()
        val password = etPassword.text?.toString().orEmpty()
        val repeat = etRepeatPassword.text?.toString().orEmpty()

        var valid = true

        if (!Validators.isValidEmail(email)) {
            tilEmail.showError(getString(R.string.auth_error_email))
            valid = false
        } else if (authRepository.isEmailRegistered(email)) {
            tilEmail.showError(getString(R.string.auth_error_email_taken))
            valid = false
        }

        if (password.isEmpty()) {
            tilPassword.showError(getString(R.string.auth_error_password_required))
            valid = false
        }

        if (repeat != password) {
            tilRepeatPassword.showError(getString(R.string.auth_error_passwords_mismatch))
            valid = false
        }

        if (!cbConsent.isChecked) {
            tvConsentError.visibility = View.VISIBLE
            valid = false
        }

        if (!valid) return

        startActivity(
            RegistrationStep2Activity.newIntent(
                this,
                RegistrationData(email = email, password = password)
            )
        )
    }
}
