package com.mtuci.drivenext.domain.model

import java.io.Serializable

/**
 * Автомобиль из каталога (таблица public.cars).
 * Serializable — чтобы передать машину в аргументах экрана «Оформление аренды».
 */
data class Car(
    val id: String,
    val brand: String,
    val model: String,
    val pricePerDay: Int,
    val transmission: Transmission,
    val fuelType: FuelType,
    val imageUrl: String?,
    val address: String,
    val description: String?
) : Serializable

enum class Transmission {
    AUTOMATIC,
    MANUAL
}

enum class FuelType {
    PETROL,
    DIESEL,
    ELECTRIC,
    HYBRID
}
