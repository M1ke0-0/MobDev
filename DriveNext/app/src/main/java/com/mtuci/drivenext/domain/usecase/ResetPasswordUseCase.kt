package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> = runRequest(networkRepository) {
        authRepository.sendPasswordReset(email.trim())
    }
}
