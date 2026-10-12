package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class GoogleSignInUseCase(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository
) {
    suspend fun start(): Result<Unit> = runRequest(networkRepository) {
        authRepository.startGoogleSignIn()
    }

    /** null — ссылка не относится к авторизации и её нужно просто проигнорировать. */
    suspend fun complete(callbackUrl: String): Result<Unit>? {
        var handled = true
        val result = runRequest(networkRepository) {
            handled = authRepository.completeOAuth(callbackUrl)
        }
        return result.takeIf { handled }
    }
}
