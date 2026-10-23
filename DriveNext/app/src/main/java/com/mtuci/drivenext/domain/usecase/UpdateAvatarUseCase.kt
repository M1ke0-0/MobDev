package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.domain.repository.NetworkRepository
import com.mtuci.drivenext.domain.repository.ProfileRepository

class UpdateAvatarUseCase(
    private val profileRepository: ProfileRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(uri: String): Result<UserProfile> = runRequest(networkRepository) {
        profileRepository.updateAvatar(uri)
    }
}
