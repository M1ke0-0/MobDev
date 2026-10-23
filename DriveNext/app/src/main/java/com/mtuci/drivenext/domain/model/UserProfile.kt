package com.mtuci.drivenext.domain.model

import java.time.LocalDate

/** Данные для экранов «Настройки», «Профиль» и для оформления аренды. */
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarUrl: String?,
    val gender: Gender?,
    val joinedAt: LocalDate?,
    /** Почта Google-аккаунта, если пользователь входил через Google. */
    val googleEmail: String?,
    /** Номер водительского удостоверения; нет, если вошли через Google без регистрации. */
    val licenseNumber: String?
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter(String::isNotBlank).joinToString(" ")
}
