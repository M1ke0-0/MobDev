package com.mtuci.drivenext.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
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
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val supabase: SupabaseProvider
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

        val avatarPath = data.avatarUri?.let { uploadPhoto(user.id, "avatar", it) }
        val licensePath = uploadPhoto(user.id, "license", data.licensePhotoUri)
        val passportPath = uploadPhoto(user.id, "passport", data.passportPhotoUri)

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

    /** Загружает фото в приватный бакет documents/<userId>/<name>.jpg и возвращает путь. */
    private suspend fun uploadPhoto(userId: String, name: String, uriString: String): String {
        val uri = Uri.parse(uriString)
        val bytes = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                null
            }
        } ?: throw AppError.FileReadFailed()

        val path = "$userId/$name.jpg"
        supabase.client.storage.from(SupabaseProvider.DOCUMENTS_BUCKET).upload(path, bytes) {
            upsert = true
            contentType = context.contentResolver.getType(uri)?.let(ContentType::parse) ?: ContentType.Image.JPEG
        }
        return path
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
