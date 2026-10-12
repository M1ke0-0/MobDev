package com.mtuci.drivenext.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Строка таблицы public.users (см. supabase/schema.sql). */
@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("middle_name") val middleName: String?,
    @SerialName("birth_date") val birthDate: String,
    val gender: String,
    @SerialName("license_number") val licenseNumber: String,
    @SerialName("license_issue_date") val licenseIssueDate: String,
    @SerialName("avatar_path") val avatarPath: String?,
    @SerialName("license_photo_path") val licensePhotoPath: String,
    @SerialName("passport_photo_path") val passportPhotoPath: String
)
