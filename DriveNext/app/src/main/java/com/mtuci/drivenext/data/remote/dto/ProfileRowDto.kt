package com.mtuci.drivenext.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Часть строки public.users, которая нужна экранам «Настройки» и «Профиль». */
@Serializable
data class ProfileRowDto(
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val gender: String,
    @SerialName("avatar_path") val avatarPath: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("license_number") val licenseNumber: String
) {
    companion object {
        const val COLUMNS = "email,first_name,last_name,gender,avatar_path,created_at,license_number"
    }
}
