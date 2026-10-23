package com.mtuci.drivenext.data.remote

import android.content.Context
import android.net.Uri
import com.mtuci.drivenext.domain.model.AppError
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Загружает фото пользователя в приватный бакет documents/<userId>/<name>.jpg. */
class PhotoUploader(
    private val context: Context,
    private val supabase: SupabaseProvider
) {
    /** Возвращает путь к файлу внутри бакета. Вызывать внутри [supabaseCall]. */
    suspend fun upload(userId: String, name: String, uriString: String): String {
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
}
