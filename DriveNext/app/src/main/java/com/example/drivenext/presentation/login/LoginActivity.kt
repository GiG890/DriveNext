package com.example.drivenext.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.data.repository.AuthRepositoryImpl
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.Validators
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.openMainAndClearStack
import com.example.drivenext.presentation.common.showError
import com.example.drivenext.presentation.common.showMessage
import com.example.drivenext.presentation.common.toMessageRes
import com.example.drivenext.presentation.register.RegistrationStep1Activity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_login)
        findViewById<View>(R.id.root).applySystemBarInsets()

        authRepository = AuthRepositoryImpl(applicationContext)

        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // «Войти» активна, только когда заполнены оба поля
        etEmail.doAfterTextChanged {
            tilEmail.showError(null)
            updateLoginButton()
        }
        etPassword.doAfterTextChanged { updateLoginButton() }

        btnLogin.setOnClickListener { attemptLogin() }

        findViewById<View>(R.id.btnGoogle).setOnClickListener { loginWithGoogle() }

        findViewById<View>(R.id.btnForgotPassword).setOnClickListener {
            showMessage(R.string.auth_forgot_password_stub)
        }

        findViewById<View>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegistrationStep1Activity::class.java))
        }
    }

    private fun updateLoginButton() {
        btnLogin.isEnabled = !etEmail.text.isNullOrBlank() && !etPassword.text.isNullOrEmpty()
    }

    private fun attemptLogin() {
        val email = etEmail.text?.toString().orEmpty().trim()
        val password = etPassword.text?.toString().orEmpty()

        if (!Validators.isValidEmail(email)) {
            tilEmail.showError(getString(R.string.auth_error_email))
            return
        }

        authRepository.login(email, password).fold(
            onSuccess = { openMainAndClearStack() },
            onFailure = { showMessage(it.toMessageRes()) }
        )
    }

    private fun loginWithGoogle() {
        authRepository.loginWithGoogle().fold(
            onSuccess = { openMainAndClearStack() },
            onFailure = { showMessage(it.toMessageRes()) }
        )
    }
}
