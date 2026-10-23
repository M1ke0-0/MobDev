package com.mtuci.drivenext.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Строка таблицы favorites: копия машины, чтобы список избранного открывался без запроса к серверу. */
@Entity(tableName = "favorites")
data class FavoriteCarEntity(
    @PrimaryKey val carId: String,
    val brand: String,
    val model: String,
    val pricePerDay: Int,
    val transmission: String,
    val fuelType: String,
    val imageUrl: String?,
    val address: String,
    val description: String?,
    val addedAt: Long
)
