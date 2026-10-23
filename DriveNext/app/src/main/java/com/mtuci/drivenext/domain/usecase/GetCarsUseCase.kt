package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.CarRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class GetCarsUseCase(
    private val carRepository: CarRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(): Result<List<Car>> = runRequest(networkRepository) {
        carRepository.getAvailableCars()
    }
}
