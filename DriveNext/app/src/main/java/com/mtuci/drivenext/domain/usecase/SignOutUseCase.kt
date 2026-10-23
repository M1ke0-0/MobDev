package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.ProfileRepository

/** Интернет не нужен: без сети выходим локально, чтобы пользователь не «застрял» в аккаунте. */
class SignOutUseCase(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
        profileRepository.clear()
    }
}
