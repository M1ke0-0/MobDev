package com.mtuci.drivenext.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mtuci.drivenext.R

data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(R.drawable.img_onboarding_1, R.string.onboarding_title_1, R.string.onboarding_description_1),
    OnboardingPage(R.drawable.img_onboarding_2, R.string.onboarding_title_2, R.string.onboarding_description_2),
    OnboardingPage(R.drawable.img_onboarding_3, R.string.onboarding_title_3, R.string.onboarding_description_3)
)
