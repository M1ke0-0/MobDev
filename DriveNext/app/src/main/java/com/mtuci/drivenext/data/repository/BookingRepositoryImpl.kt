package com.mtuci.drivenext.data.repository

import com.mtuci.drivenext.data.local.BookingDao
import com.mtuci.drivenext.data.local.BookingEntity
import com.mtuci.drivenext.domain.model.Booking
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.model.FuelType
import com.mtuci.drivenext.domain.model.Transmission
import com.mtuci.drivenext.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingRepositoryImpl(
    private val dao: BookingDao
) : BookingRepository {

    override fun getBookings(): Flow<List<Booking>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getBooking(id: Long): Booking? = dao.getById(id)?.toDomain()

    override suspend fun create(booking: Booking): Long = dao.insert(
        BookingEntity(
            carId = booking.car.id,
            brand = booking.car.brand,
            model = booking.car.model,
            pricePerDay = booking.car.pricePerDay,
            transmission = booking.car.transmission.name,
            fuelType = booking.car.fuelType.name,
            imageUrl = booking.car.imageUrl,
            address = booking.car.address,
            startAt = booking.start.toEpochSecond(ZoneOffset.UTC),
            endAt = booking.end.toEpochSecond(ZoneOffset.UTC),
            driverName = booking.driverName,
            licenseNumber = booking.licenseNumber
        )
    )

    override suspend fun cancel(id: Long) = dao.delete(id)

    private fun BookingEntity.toDomain() = Booking(
        id = id,
        car = Car(
            id = carId,
            brand = brand,
            model = model,
            pricePerDay = pricePerDay,
            transmission = Transmission.valueOf(transmission),
            fuelType = FuelType.valueOf(fuelType),
            imageUrl = imageUrl,
            address = address,
            description = null
        ),
        start = LocalDateTime.ofEpochSecond(startAt, 0, ZoneOffset.UTC),
        end = LocalDateTime.ofEpochSecond(endAt, 0, ZoneOffset.UTC),
        driverName = driverName,
        licenseNumber = licenseNumber
    )
}
