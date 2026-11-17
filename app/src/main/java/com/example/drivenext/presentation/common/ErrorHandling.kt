package com.example.drivenext.presentation.common

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.drivenext.presentation.noconnection.NoConnectionActivity

/**
 * Реакция экрана на ошибку загрузки по методичке:
 * нет интернета, значит экран No Connection; ошибка сервера: текст в диалоговом окне.
 * Под диалогом на экране остаётся сообщение и кнопка «Повторить».
 */
fun Fragment.showLoadError(error: UiError) {
    if (error.isNoConnection) {
        startActivity(Intent(requireContext(), NoConnectionActivity::class.java))
    } else {
        requireActivity().showErrorDialog(error)
    }
}
