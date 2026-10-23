package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class SignInUseCase(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> = runRequest(networkRepository) {
        authRepository.signIn(email.trim(), password)
    }
}
