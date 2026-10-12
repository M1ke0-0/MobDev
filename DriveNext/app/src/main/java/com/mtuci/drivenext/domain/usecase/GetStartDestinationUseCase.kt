package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.StartDestination
import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.OnboardingRepository

class GetStartDestinationUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StartDestination = when {
        !onboardingRepository.isOnboardingCompleted() -> StartDestination.ONBOARDING
        !authRepository.hasValidAccessToken() -> StartDestination.AUTH
        else -> StartDestination.MAIN
    }
}
