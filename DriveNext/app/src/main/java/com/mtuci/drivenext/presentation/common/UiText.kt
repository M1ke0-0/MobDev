package com.mtuci.drivenext.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import com.mtuci.drivenext.R
import com.mtuci.drivenext.domain.model.AppError

/** Текст для UI, который ViewModel может сформировать без доступа к Context. */
class UiText(@StringRes val resId: Int, private vararg val args: Any) {
    fun resolve(context: Context): String = context.getString(resId, *args)
}

fun Throwable.toUiText(): UiText = when (this) {
    is AppError.NoConnection -> UiText(R.string.error_no_connection)
    is AppError.NotConfigured -> UiText(R.string.error_not_configured)
    is AppError.InvalidCredentials -> UiText(R.string.error_invalid_credentials)
    is AppError.EmailNotConfirmed -> UiText(R.string.error_email_not_confirmed)
    is AppError.UserAlreadyExists -> UiText(R.string.error_user_exists)
    is AppError.WeakPassword -> UiText(R.string.error_weak_password)
    is AppError.TooManyRequests -> UiText(R.string.error_too_many_requests)
    is AppError.FileReadFailed -> UiText(R.string.error_file_read)
    is AppError.Server -> details?.let { UiText(R.string.error_server, it) } ?: UiText(R.string.error_unknown)
    else -> UiText(R.string.error_unknown)
}
