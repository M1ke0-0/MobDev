package com.mtuci.drivenext.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Строка таблицы bookings. Даты — секунды с 1970 года (Room хранит только простые типы). */
@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val carId: String,
    val brand: String,
    val model: String,
    val pricePerDay: Int,
    val transmission: String,
    val fuelType: String,
    val imageUrl: String?,
    val address: String,
    val startAt: Long,
    val endAt: Long,
    val driverName: String,
    val licenseNumber: String
)
