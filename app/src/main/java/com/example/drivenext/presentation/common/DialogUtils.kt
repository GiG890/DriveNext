package com.example.drivenext.presentation.common

import android.app.Activity
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator

/** Показывает текст ошибки в диалоговом окне (требование методички). */
fun Activity.showErrorDialog(@StringRes messageRes: Int) {
    MaterialAlertDialogBuilder(this)
        .setMessage(messageRes)
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

/** Диалог с текстом ошибки и, если сервер что-то вернул, с его сообщением. */
fun Activity.showErrorDialog(error: UiError) {
    val text = if (error.details.isNullOrBlank()) {
        getString(error.messageRes)
    } else {
        getString(error.messageRes) + "\n\n" + error.details
    }
    MaterialAlertDialogBuilder(this)
        .setMessage(text)
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

/** Информационное сообщение, например «Раздел в разработке». */
fun Activity.showInfoDialog(@StringRes messageRes: Int) = showErrorDialog(messageRes)

/** Индикатор выполнения запроса: небольшой диалог с крутилкой, который нельзя закрыть вручную. */
class LoadingDialog(private val activity: Activity) {

    private var dialog: AlertDialog? = null

    fun setVisible(visible: Boolean) {
        if (!visible) {
            dismiss()
            return
        }
        if (dialog?.isShowing == true || activity.isFinishing || activity.isDestroyed) return

        val padding = (24 * activity.resources.displayMetrics.density).toInt()
        val container = FrameLayout(activity).apply {
            setPadding(padding, padding, padding, padding)
            addView(CircularProgressIndicator(activity).apply { isIndeterminate = true })
        }
        dialog = MaterialAlertDialogBuilder(activity)
            .setView(container)
            .setCancelable(false)
            .show()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}
