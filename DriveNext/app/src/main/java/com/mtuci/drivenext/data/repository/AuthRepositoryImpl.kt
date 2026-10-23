package com.mtuci.drivenext.data.repository

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.mtuci.drivenext.data.remote.PhotoUploader
import com.mtuci.drivenext.data.remote.SupabaseProvider
import com.mtuci.drivenext.data.remote.dto.UserProfileDto
import com.mtuci.drivenext.data.remote.supabaseCall
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.Gender
import com.mtuci.drivenext.domain.model.RegistrationData
import com.mtuci.drivenext.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val prefs: SharedPreferences,
    private val supabase: SupabaseProvider,
    private val photoUploader: PhotoUploader
) : AuthRepository {

    override fun hasValidAccessToken(): Boolean =
        !prefs.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank()

    override suspend fun signIn(email: String, password: String) = supabaseCall {
        supabase.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        saveSession()
    }

    override suspend fun startGoogleSignIn() = supabaseCall {
        // Откроет браузер; после входа Google вернёт пользователя по диплинку в LoginActivity.
        supabase.client.auth.signInWith(Google)
    }

    override suspend fun completeOAuth(callbackUrl: String): Boolean = supabaseCall {
        val uri = Uri.parse(callbackUrl)
        if (uri.scheme != SupabaseProvider.DEEPLINK_SCHEME || uri.host != SupabaseProvider.DEEPLINK_HOST) {
            return@supabaseCall false
        }
        uri.getQueryParameter("error")?.let { error ->
            throw AppError.Server(uri.getQueryParameter("error_description") ?: error)
        }
        val code = uri.getQueryParameter("code") ?: throw AppError.Server("Нет кода авторизации")
        // PKCE: меняем одноразовый код на сессию (access + refresh token)
        supabase.client.auth.exchangeCodeForSession(code)
        saveSession()
        true
    }

    override suspend fun sendPasswordReset(email: String) = supabaseCall {
        supabase.client.auth.resetPasswordForEmail(email)
    }

    override suspend fun register(data: RegistrationData) = supabaseCall {
        val client = supabase.client
        client.auth.signUpWith(Email) {
            email = data.email
            password = data.password
            this.data = buildJsonObject {
                put("first_name", data.firstName)
                put("last_name", data.lastName)
            }
        }
        // Если в Supabase включено подтверждение почты, сессии после signUp не будет.
        val user = client.auth.currentUserOrNull() ?: throw AppError.EmailNotConfirmed()

        val avatarPath = data.avatarUri?.let { photoUploader.upload(user.id, "avatar", it) }
        val licensePath = photoUploader.upload(user.id, "license", data.licensePhotoUri)
        val passportPath = photoUploader.upload(user.id, "passport", data.passportPhotoUri)

        client.postgrest[SupabaseProvider.USERS_TABLE].upsert(
            UserProfileDto(
                id = user.id,
                email = data.email,
                lastName = data.lastName,
                firstName = data.firstName,
                middleName = data.middleName,
                birthDate = data.birthDate.toString(),
                gender = data.gender.toDb(),
                licenseNumber = data.licenseNumber,
                licenseIssueDate = data.licenseIssueDate.toString(),
                avatarPath = avatarPath,
                licensePhotoPath = licensePath,
                passportPhotoPath = passportPath
            )
        )
        saveSession()
    }

    override suspend fun signOut() {
        try {
            supabaseCall { supabase.client.auth.signOut() }
        } catch (e: AppError) {
            // Сервер недоступен — всё равно забываем сессию на устройстве
            runCatching { supabase.client.auth.clearSession() }
        }
        prefs.edit { remove(KEY_ACCESS_TOKEN) }
    }

    private fun saveSession() {
        val token = supabase.client.auth.currentSessionOrNull()?.accessToken
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    private fun Gender.toDb(): String = when (this) {
        Gender.MALE -> "male"
        Gender.FEMALE -> "female"
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
