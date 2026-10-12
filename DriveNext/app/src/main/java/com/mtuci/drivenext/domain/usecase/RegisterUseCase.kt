package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.RegistrationData
import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class RegisterUseCase(
    private val authRepository: AuthRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(data: RegistrationData): Result<Unit> = runRequest(networkRepository) {
        authRepository.register(data.copy(email = data.email.trim()))
    }
}
