package com.mtuci.drivenext.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Строка таблицы public.cars (см. supabase/schema_lab3.sql). */
@Serializable
data class CarDto(
    val id: String,
    val brand: String,
    val model: String,
    @SerialName("price_per_day") val pricePerDay: Int,
    val transmission: String,
    @SerialName("fuel_type") val fuelType: String,
    @SerialName("image_path") val imagePath: String?,
    val address: String,
    val description: String?
) {
    companion object {
        /** Явный список колонок: лишние поля из таблицы не сломают разбор JSON. */
        const val COLUMNS = "id,brand,model,price_per_day,transmission,fuel_type,image_path,address,description"
    }
}
