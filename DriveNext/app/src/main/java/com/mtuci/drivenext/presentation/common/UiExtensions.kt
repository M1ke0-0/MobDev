package com.mtuci.drivenext.presentation.common

import android.content.Context
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.mtuci.drivenext.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Подписка на Flow, которая активна только пока экран виден (STARTED). */
fun <T> LifecycleOwner.collectWhenStarted(flow: Flow<T>, action: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

/** Ошибки сервера по требованиям курса показываем в диалоговом окне. */
fun Context.showErrorDialog(message: UiText) {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.error_dialog_title)
        .setMessage(message.resolve(this))
        .setPositiveButton(R.string.ok, null)
        .show()
}

/** Заполняет поле значением из ViewModel, например когда шаг регистрации открыт заново. */
fun EditText.setTextIfChanged(value: String) {
    if (text.toString() != value) setText(value)
}

/** Показывает ошибку под полем; без ошибки убирает и пустое место под неё. */
fun TextInputLayout.showError(message: String?) {
    error = message
    isErrorEnabled = message != null
}
