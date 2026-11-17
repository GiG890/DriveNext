package com.example.drivenext.presentation.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.R
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.login.LoginActivity
import com.example.drivenext.presentation.register.RegistrationStep1Activity

/** Экран выбора действия: войти или зарегистрироваться. */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_welcome)
        findViewById<View>(R.id.root).applySystemBarInsets()

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<View>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegistrationStep1Activity::class.java))
        }
    }
}
