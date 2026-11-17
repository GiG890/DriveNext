package com.example.drivenext.presentation.common

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.drivenext.MainActivity
import com.example.drivenext.presentation.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max

/**
 * Edge-to-edge со светлыми иконками статус-бара. targetSdk = 36 включает edge-to-edge
 * принудительно, поэтому без отступов контент уезжает под статус-бар.
 * Вызывать до setContentView().
 */
fun ComponentActivity.enableEdgeToEdgeLight() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    )
}

/** Добавляет к padding корня отступы системных панелей и клавиатуры. */
fun View.applySystemBarInsets() {
    val left = paddingLeft
    val top = paddingTop
    val right = paddingRight
    val bottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
        insets
    }
}

/** Показывает или убирает ошибку под полем. При null схлопывает место под текст ошибки. */
fun TextInputLayout.showError(message: String?) {
    if (message == null) {
        error = null
        isErrorEnabled = false
    } else {
        isErrorEnabled = true
        error = message
    }
}

/** Короткая подсказка внизу экрана (для не-ошибок). Ошибки показываем диалогом: showErrorDialog. */
fun Activity.showMessage(@StringRes messageRes: Int) {
    Snackbar.make(findViewById(android.R.id.content), messageRes, Snackbar.LENGTH_LONG).show()
}

/** Открывает главный экран и очищает стек навигации (назад из него уже не вернуться к входу). */
fun Activity.openMainAndClearStack() {
    startActivity(
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}

/** Открывает экран входа и очищает стек навигации (после выхода из профиля). */
fun Activity.openLoginAndClearStack() {
    startActivity(
        Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}

/**
 * Показывает картинку по Uri, уменьшая её при декодировании (снимок с камеры может быть
 * на 12+ Мпикс). Возвращает false, если файл не удалось открыть.
 */
fun ImageView.setImageFromUri(uri: Uri, maxSidePx: Int = 512): Boolean = try {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val side = max(info.size.width, info.size.height)
        decoder.setTargetSampleSize(max(1, side / maxSidePx))
    }
    setImageBitmap(bitmap)
    true
} catch (e: Exception) {
    false
}
