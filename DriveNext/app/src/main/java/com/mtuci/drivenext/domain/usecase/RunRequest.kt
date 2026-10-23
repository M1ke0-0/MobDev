package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.AppError
import com.mtuci.drivenext.domain.repository.NetworkRepository

/** Общая обёртка для запросов: сначала проверяем интернет, ошибки превращаем в Result. */
internal suspend fun <T> runRequest(networkRepository: NetworkRepository, block: suspend () -> T): Result<T> {
    if (!networkRepository.isInternetAvailable()) return Result.failure(AppError.NoConnection())
    return try {
        Result.success(block())
    } catch (e: AppError) {
        Result.failure(e)
    }
}
