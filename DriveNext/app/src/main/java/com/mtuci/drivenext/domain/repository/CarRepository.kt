package com.mtuci.drivenext.domain.repository

import com.mtuci.drivenext.domain.model.Car

/** Все методы при ошибке бросают [com.mtuci.drivenext.domain.model.AppError]. */
interface CarRepository {
    suspend fun getAvailableCars(): List<Car>

    /** Ищет по марке и модели; каждое слово запроса должно встретиться в марке или модели. */
    suspend fun searchCars(query: String): List<Car>

    /** Одна машина для экрана «Детали». */
    suspend fun getCar(id: String): Car
}
