package com.mtuci.drivenext.domain.model

import java.time.LocalDate

/**
 * Всё, что пользователь ввёл на трёх шагах регистрации.
 * Фото передаются строковыми Uri, чтобы domain-слой не зависел от Android.
 */
data class RegistrationData(
    val email: String,
    val password: String,
    val lastName: String,
    val firstName: String,
    val middleName: String?,
    val birthDate: LocalDate,
    val gender: Gender,
    val licenseNumber: String,
    val licenseIssueDate: LocalDate,
    val avatarUri: String?,
    val licensePhotoUri: String,
    val passportPhotoUri: String
)
