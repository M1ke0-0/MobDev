package com.mtuci.drivenext.data.repository

import com.mtuci.drivenext.data.local.FavoriteCarEntity
import com.mtuci.drivenext.data.local.FavoriteDao
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.model.FuelType
import com.mtuci.drivenext.domain.model.Transmission
import com.mtuci.drivenext.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteDao
) : FavoritesRepository {

    override fun getFavorites(): Flow<List<Car>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun isFavorite(carId: String): Flow<Boolean> = dao.isFavorite(carId)

    override suspend fun add(car: Car) = dao.insert(
        FavoriteCarEntity(
            carId = car.id,
            brand = car.brand,
            model = car.model,
            pricePerDay = car.pricePerDay,
            transmission = car.transmission.name,
            fuelType = car.fuelType.name,
            imageUrl = car.imageUrl,
            address = car.address,
            description = car.description,
            addedAt = System.currentTimeMillis()
        )
    )

    override suspend fun remove(carId: String) = dao.delete(carId)

    private fun FavoriteCarEntity.toDomain() = Car(
        id = carId,
        brand = brand,
        model = model,
        pricePerDay = pricePerDay,
        transmission = Transmission.valueOf(transmission),
        fuelType = FuelType.valueOf(fuelType),
        imageUrl = imageUrl,
        address = address,
        description = description
    )
}
