package com.mtuci.drivenext.data.remote

import com.mtuci.drivenext.domain.model.AppError
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Выполняет запрос к Supabase и переводит исключения библиотеки в [AppError].
 * Отмену корутины пробрасываем как есть — это не ошибка.
 */
internal suspend fun <T> supabaseCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    throw e.toAppError()
}

private fun Exception.toAppError(): AppError = when (this) {
    is AppError -> this
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AppError.InvalidCredentials()
        AuthErrorCode.EmailNotConfirmed -> AppError.EmailNotConfirmed()
        AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> AppError.UserAlreadyExists()
        AuthErrorCode.WeakPassword -> AppError.WeakPassword()
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> AppError.TooManyRequests()
        else -> AppError.Server(errorDescription)
    }
    is RestException -> AppError.Server(description ?: error)
    // HttpRequestException и таймауты Ktor — наследники IOException
    is IOException -> AppError.NoConnection()
    else -> AppError.Unknown(message)
}
