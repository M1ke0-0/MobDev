package com.mtuci.drivenext.domain.repository

import com.mtuci.drivenext.domain.model.Car
import kotlinx.coroutines.flow.Flow

/** Избранные машины хранятся на устройстве (Room). */
interface FavoritesRepository {
    /** Список обновляется сам, когда машину добавили или убрали из избранного. */
    fun getFavorites(): Flow<List<Car>>

    fun isFavorite(carId: String): Flow<Boolean>

    suspend fun add(car: Car)

    suspend fun remove(carId: String)
}
