package com.mtuci.drivenext.data.repository

import com.mtuci.drivenext.data.remote.PhotoUploader
import com.mtuci.drivenext.data.remote.SupabaseProvider
import com.mtuci.drivenext.data.remote.dto.ProfileRowDto
import com.mtuci.drivenext.data.remote.supabaseCall
import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.model.Gender
import com.mtuci.drivenext.domain.model.UserProfile
import com.mtuci.drivenext.domain.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class ProfileRepositoryImpl(
    private val supabase: SupabaseProvider,
    private val photoUploader: PhotoUploader
) : ProfileRepository {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    override val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    override suspend fun refreshProfile(): UserProfile = supabaseCall {
        val user = currentUser()
        val row = loadRow(user.id)
        buildProfile(user, row).also { _profile.value = it }
    }

    override suspend fun updateAvatar(uri: String): UserProfile = supabaseCall {
        val user = currentUser()
        val path = photoUploader.upload(user.id, AVATAR_FILE, uri)
        if (loadRow(user.id) != null) {
            supabase.client.postgrest[SupabaseProvider.USERS_TABLE].update({ set("avatar_path", path) }) {
                filter { eq("id", user.id) }
            }
        } else {
            // Вошли через Google и не заполняли регистрацию — строки в users нет, храним путь в метаданных
            supabase.client.auth.updateUser { data { put(META_AVATAR_PATH, path) } }
        }
        val updatedUser = supabase.client.auth.currentUserOrNull() ?: user
        buildProfile(updatedUser, loadRow(user.id)).also { _profile.value = it }
    }

    override fun clear() {
        _profile.value = null
    }

    /** Сессия хранится на диске; после запуска приложения её чтение асинхронное — дожидаемся. */
    private suspend fun currentUser(): UserInfo {
        val auth = supabase.client.auth
        auth.awaitInitialization()
        return auth.currentUserOrNull() ?: throw AppError.SessionExpired()
    }

    private suspend fun loadRow(userId: String): ProfileRowDto? =
        supabase.client.postgrest[SupabaseProvider.USERS_TABLE]
            .select(Columns.raw(ProfileRowDto.COLUMNS)) { filter { eq("id", userId) } }
            .decodeSingleOrNull<ProfileRowDto>()

    // user.createdAt — kotlin.time.Instant, он пока экспериментальный
    @OptIn(ExperimentalTime::class)
    private suspend fun buildProfile(user: UserInfo, row: ProfileRowDto?): UserProfile {
        val meta = user.userMetadata
        val avatarPath = row?.avatarPath ?: meta.string(META_AVATAR_PATH)
        return UserProfile(
            firstName = row?.firstName ?: meta.string("first_name") ?: meta.string("full_name") ?: meta.string("name").orEmpty(),
            lastName = row?.lastName ?: meta.string("last_name").orEmpty(),
            email = row?.email ?: user.email.orEmpty(),
            // Аватар из Google — обычная публичная ссылка, свой — в приватном бакете, нужна подписанная ссылка
            avatarUrl = avatarPath?.let { signedUrl(it) } ?: meta.string("avatar_url"),
            gender = when (row?.gender) {
                "male" -> Gender.MALE
                "female" -> Gender.FEMALE
                else -> null
            },
            joinedAt = parseDate(row?.createdAt ?: user.createdAt?.toString()),
            googleEmail = user.identities
                ?.firstOrNull { it.provider == "google" }
                ?.identityData?.string("email"),
            licenseNumber = row?.licenseNumber
        )
    }

    /** Если ссылку получить не удалось, показываем заглушку вместо аватара, а не ошибку на весь экран. */
    private suspend fun signedUrl(path: String): String? = try {
        supabase.client.storage.from(SupabaseProvider.DOCUMENTS_BUCKET).createSignedUrl(path, 1.hours)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun JsonObject?.string(key: String): String? =
        this?.get(key)?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)

    private fun parseDate(value: String?): LocalDate? =
        value?.let { runCatching { OffsetDateTime.parse(it).toLocalDate() }.getOrNull() }

    private companion object {
        const val AVATAR_FILE = "avatar"
        const val META_AVATAR_PATH = "avatar_path"
    }
}
