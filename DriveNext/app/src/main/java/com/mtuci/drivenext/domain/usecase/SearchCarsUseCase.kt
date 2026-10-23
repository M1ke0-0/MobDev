package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.CarRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class SearchCarsUseCase(
    private val carRepository: CarRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(query: String): Result<List<Car>> = runRequest(networkRepository) {
        carRepository.searchCars(query.trim())
    }
}
