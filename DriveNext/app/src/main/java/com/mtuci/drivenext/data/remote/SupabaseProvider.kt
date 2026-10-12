package com.mtuci.drivenext.data.remote

import com.mtuci.drivenext.BuildConfig
import com.mtuci.drivenext.domain.model.AppError
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Единственный клиент Supabase на всё приложение.
 * Создаётся лениво: если ключи в local.properties не заданы, приложение не падает,
 * а запросы возвращают понятную ошибку [AppError.NotConfigured].
 */
class SupabaseProvider {

    val isConfigured: Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_KEY.isNotBlank()

    private val lazyClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                // Куда Google вернёт пользователя после входа: drivenext://login-callback
                scheme = DEEPLINK_SCHEME
                host = DEEPLINK_HOST
                flowType = FlowType.PKCE
            }
            install(Postgrest)
            install(Storage)
        }
    }

    val client: SupabaseClient
        get() = if (isConfigured) lazyClient else throw AppError.NotConfigured()

    companion object {
        const val DEEPLINK_SCHEME = "drivenext"
        const val DEEPLINK_HOST = "login-callback"

        const val USERS_TABLE = "users"
        const val DOCUMENTS_BUCKET = "documents"
    }
}
