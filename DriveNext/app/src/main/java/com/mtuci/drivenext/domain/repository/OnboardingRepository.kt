package com.mtuci.drivenext.domain.repository

interface OnboardingRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted()
}
