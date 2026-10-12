package com.mtuci.drivenext.di

import android.content.Context
import androidx.core.content.edit
import com.mtuci.drivenext.data.remote.SupabaseProvider
import com.mtuci.drivenext.data.repository.AuthRepositoryImpl
import com.mtuci.drivenext.data.repository.NetworkRepositoryImpl
import com.mtuci.drivenext.data.repository.OnboardingRepositoryImpl
import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository
import com.mtuci.drivenext.domain.repository.OnboardingRepository
import com.mtuci.drivenext.domain.usecase.GetStartDestinationUseCase
import com.mtuci.drivenext.domain.usecase.GoogleSignInUseCase
import com.mtuci.drivenext.domain.usecase.RegisterUseCase
import com.mtuci.drivenext.domain.usecase.ResetPasswordUseCase
import com.mtuci.drivenext.domain.usecase.SignInUseCase

class AppContainer(context: Context) {

    private val prefs = context.getSharedPreferences("drivenext_prefs", Context.MODE_PRIVATE)
    private val supabaseProvider = SupabaseProvider()

    val networkRepository: NetworkRepository = NetworkRepositoryImpl(context)
    val onboardingRepository: OnboardingRepository = OnboardingRepositoryImpl(prefs)
    private val authRepository: AuthRepository = AuthRepositoryImpl(context, prefs, supabaseProvider)

    val getStartDestinationUseCase = GetStartDestinationUseCase(onboardingRepository, authRepository)
    val signInUseCase = SignInUseCase(authRepository, networkRepository)
    val googleSignInUseCase = GoogleSignInUseCase(authRepository, networkRepository)
    val resetPasswordUseCase = ResetPasswordUseCase(authRepository, networkRepository)
    val registerUseCase = RegisterUseCase(authRepository, networkRepository)

    // Возвращает приложение в состояние «первый запуск»: онбординг и вход покажутся снова.
    fun resetAppData() {
        prefs.edit(commit = true) { clear() }
    }
}
