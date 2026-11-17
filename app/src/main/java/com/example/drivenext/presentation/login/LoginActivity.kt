package com.example.drivenext.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.presentation.common.AppViewModelFactory
import com.example.drivenext.presentation.common.LoadingDialog
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.openMainAndClearStack
import com.example.drivenext.presentation.common.showError
import com.example.drivenext.presentation.common.showErrorDialog
import com.example.drivenext.presentation.common.showMessage
import com.example.drivenext.presentation.register.RegistrationStep1Activity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { AppViewModelFactory(this) }
    private val loadingDialog by lazy { LoadingDialog(this) }

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_login)
        findViewById<View>(R.id.root).applySystemBarInsets()

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        etEmail.doAfterTextChanged {
            tilEmail.showError(null)
            notifyFieldsChanged()
        }
        etPassword.doAfterTextChanged { notifyFieldsChanged() }

        btnLogin.setOnClickListener {
            viewModel.onLoginClicked(
                etEmail.text?.toString().orEmpty(),
                etPassword.text?.toString().orEmpty()
            )
        }
        findViewById<View>(R.id.btnGoogle).setOnClickListener { viewModel.onGoogleClicked() }

        findViewById<View>(R.id.btnForgotPassword).setOnClickListener {
            showMessage(R.string.auth_forgot_password_stub)
        }
        findViewById<View>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegistrationStep1Activity::class.java))
        }

        viewModel.loginEnabled.observe(this) { btnLogin.isEnabled = it }
        viewModel.loading.observe(this) { loadingDialog.setVisible(it) }
        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let { handle(it) }
        }
    }

    override fun onDestroy() {
        loadingDialog.dismiss()
        super.onDestroy()
    }

    private fun notifyFieldsChanged() {
        viewModel.onFieldsChanged(
            etEmail.text?.toString().orEmpty(),
            etPassword.text?.toString().orEmpty()
        )
    }

    private fun handle(event: LoginEvent) {
        when (event) {
            is LoginEvent.InvalidEmail -> tilEmail.showError(getString(R.string.auth_error_email))
            is LoginEvent.LoggedIn -> openMainAndClearStack()
            is LoginEvent.Failure -> showErrorDialog(event.error)
        }
    }
}
