package com.mtuci.drivenext.di

import android.content.Context
import androidx.core.content.edit
import com.mtuci.drivenext.data.local.AppDatabase
import com.mtuci.drivenext.data.remote.PhotoUploader
import com.mtuci.drivenext.data.remote.SupabaseProvider
import com.mtuci.drivenext.data.repository.AuthRepositoryImpl
import com.mtuci.drivenext.data.repository.BookingRepositoryImpl
import com.mtuci.drivenext.data.repository.CarRepositoryImpl
import com.mtuci.drivenext.data.repository.FavoritesRepositoryImpl
import com.mtuci.drivenext.data.repository.NetworkRepositoryImpl
import com.mtuci.drivenext.data.repository.OnboardingRepositoryImpl
import com.mtuci.drivenext.data.repository.ProfileRepositoryImpl
import com.mtuci.drivenext.domain.repository.AuthRepository
import com.mtuci.drivenext.domain.repository.BookingRepository
import com.mtuci.drivenext.domain.repository.CarRepository
import com.mtuci.drivenext.domain.repository.FavoritesRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository
import com.mtuci.drivenext.domain.repository.OnboardingRepository
import com.mtuci.drivenext.domain.repository.ProfileRepository
import com.mtuci.drivenext.domain.usecase.GetCarDetailsUseCase
import com.mtuci.drivenext.domain.usecase.GetCarsUseCase
import com.mtuci.drivenext.domain.usecase.GetProfileUseCase
import com.mtuci.drivenext.domain.usecase.GetStartDestinationUseCase
import com.mtuci.drivenext.domain.usecase.GoogleSignInUseCase
import com.mtuci.drivenext.domain.usecase.RegisterUseCase
import com.mtuci.drivenext.domain.usecase.ResetPasswordUseCase
import com.mtuci.drivenext.domain.usecase.SearchCarsUseCase
import com.mtuci.drivenext.domain.usecase.SignInUseCase
import com.mtuci.drivenext.domain.usecase.SignOutUseCase
import com.mtuci.drivenext.domain.usecase.UpdateAvatarUseCase

class AppContainer(context: Context) {

    private val prefs = context.getSharedPreferences("drivenext_prefs", Context.MODE_PRIVATE)
    private val supabaseProvider = SupabaseProvider()
    private val photoUploader = PhotoUploader(context, supabaseProvider)
    private val database = AppDatabase.getInstance(context)

    val networkRepository: NetworkRepository = NetworkRepositoryImpl(context)
    val onboardingRepository: OnboardingRepository = OnboardingRepositoryImpl(prefs)
    private val authRepository: AuthRepository = AuthRepositoryImpl(prefs, supabaseProvider, photoUploader)
    private val carRepository: CarRepository = CarRepositoryImpl(supabaseProvider)
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(supabaseProvider, photoUploader)

    // Избранное и бронирования — локальные данные, сеть не нужна, поэтому ViewModel работают с ними напрямую
    val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl(database.favoriteDao())
    val bookingRepository: BookingRepository = BookingRepositoryImpl(database.bookingDao())

    val getStartDestinationUseCase = GetStartDestinationUseCase(onboardingRepository, authRepository)
    val signInUseCase = SignInUseCase(authRepository, networkRepository)
    val googleSignInUseCase = GoogleSignInUseCase(authRepository, networkRepository)
    val resetPasswordUseCase = ResetPasswordUseCase(authRepository, networkRepository)
    val registerUseCase = RegisterUseCase(authRepository, networkRepository)

    val getCarsUseCase = GetCarsUseCase(carRepository, networkRepository)
    val searchCarsUseCase = SearchCarsUseCase(carRepository, networkRepository)
    val getCarDetailsUseCase = GetCarDetailsUseCase(carRepository, networkRepository)
    val getProfileUseCase = GetProfileUseCase(profileRepository, networkRepository)
    val updateAvatarUseCase = UpdateAvatarUseCase(profileRepository, networkRepository)
    val signOutUseCase = SignOutUseCase(authRepository, profileRepository)

    // Возвращает приложение в состояние «первый запуск»: онбординг и вход покажутся снова.
    fun resetAppData() {
        prefs.edit(commit = true) { clear() }
    }
}
