package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.AppError
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

/** Общая обёртка для запросов: сначала проверяем интернет, ошибки превращаем в Result. */
internal suspend fun runRequest(networkRepository: NetworkRepository, block: suspend () -> Unit): Result<Unit> {
    if (!networkRepository.isInternetAvailable()) return Result.failure(AppError.NoConnection())
    return try {
        block()
        Result.success(Unit)
    } catch (e: AppError) {
        Result.failure(e)
    }
}
