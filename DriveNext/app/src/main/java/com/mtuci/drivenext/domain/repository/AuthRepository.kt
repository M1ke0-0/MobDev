package com.mtuci.drivenext.domain.repository

import com.mtuci.drivenext.domain.model.RegistrationData

/** Все методы при ошибке бросают [com.mtuci.drivenext.domain.model.AppError]. */
interface AuthRepository {
    fun hasValidAccessToken(): Boolean

    suspend fun signIn(email: String, password: String)

    /** Открывает в браузере страницу входа Google. Результат придёт диплинком в [completeOAuth]. */
    suspend fun startGoogleSignIn()

    /** Обрабатывает диплинк drivenext://login-callback. Возвращает false, если ссылка не наша. */
    suspend fun completeOAuth(callbackUrl: String): Boolean

    suspend fun sendPasswordReset(email: String)

    suspend fun register(data: RegistrationData)
}
