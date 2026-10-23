package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.domain.repository.NetworkRepository
import com.mtuci.drivenext.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.StateFlow

class GetProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val networkRepository: NetworkRepository
) {
    val profile: StateFlow<UserProfile?> = profileRepository.profile

    suspend fun refresh(): Result<UserProfile> = runRequest(networkRepository) {
        profileRepository.refreshProfile()
    }
}
