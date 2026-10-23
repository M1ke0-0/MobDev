package com.mtuci.drivenext.domain.repository

import com.mtuci.drivenext.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/** Все suspend-методы при ошибке бросают [com.mtuci.drivenext.domain.model.AppError]. */
interface ProfileRepository {
    /** Последний загруженный профиль: «Настройки» и «Профиль» показывают одни и те же данные. */
    val profile: StateFlow<UserProfile?>

    suspend fun refreshProfile(): UserProfile

    /** Загружает новое фото в хранилище и сохраняет его в профиле. */
    suspend fun updateAvatar(uri: String): UserProfile

    fun clear()
}
