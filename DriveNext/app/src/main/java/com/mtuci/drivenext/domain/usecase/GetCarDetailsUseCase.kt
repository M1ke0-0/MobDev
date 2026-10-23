package com.mtuci.drivenext.domain.usecase

import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.repository.CarRepository
import com.mtuci.drivenext.domain.repository.NetworkRepository

class GetCarDetailsUseCase(
    private val carRepository: CarRepository,
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(carId: String): Result<Car> = runRequest(networkRepository) {
        carRepository.getCar(carId)
    }
}
