package com.example.drivenext.presentation.register

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.showError
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Регистрация, шаг 1: почта, пароль, согласие с условиями. */
class RegistrationStep1Activity : AppCompatActivity() {

    private val viewModel: RegistrationStep1ViewModel by viewModels()

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

        findViewById<View>(R.id.btnNext).setOnClickListener {
            viewModel.onNextClicked(
                email = etEmail.text?.toString().orEmpty(),
                password = etPassword.text?.toString().orEmpty(),
                repeatPassword = etRepeatPassword.text?.toString().orEmpty(),
                consentChecked = cbConsent.isChecked
            )
        }

        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let { handle(it) }
        }
    }

    private fun handle(event: Step1Event) {
        when (event) {
            is Step1Event.ShowErrors -> {
                val errors = event.errors
                tilEmail.showError(errors.emailRes?.let { getString(it) })
                tilPassword.showError(errors.passwordRes?.let { getString(it) })
                tilRepeatPassword.showError(errors.repeatRes?.let { getString(it) })
                tvConsentError.visibility = if (errors.consentMissing) View.VISIBLE else View.GONE
            }
            is Step1Event.Proceed -> startActivity(
                RegistrationStep2Activity.newIntent(this, event.data)
            )
        }
    }
}
