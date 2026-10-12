package com.mtuci.drivenext.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.mtuci.drivenext.domain.repository.OnboardingRepository

class OnboardingRepositoryImpl(
    private val prefs: SharedPreferences
) : OnboardingRepository {

    override fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted() {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, true) }
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
