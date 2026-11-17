package com.example.drivenext.presentation.register

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.drivenext.R
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.openMainAndClearStack

/** Экран «Поздравляем» после регистрации. */
class RegistrationSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_registration_success)
        findViewById<View>(R.id.root).applySystemBarInsets()

        findViewById<View>(R.id.btnNext).setOnClickListener { openMainAndClearStack() }

        // Назад отсюда в регистрацию не возвращаемся: ведёт туда же, куда «Далее»
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = openMainAndClearStack()
        })
    }
}
