package com.mtuci.drivenext.data.repository

import com.mtuci.drivenext.data.remote.SupabaseProvider
import com.mtuci.drivenext.data.remote.dto.CarDto
import com.mtuci.drivenext.data.remote.supabaseCall
import com.mtuci.drivenext.domain.model.Car
import com.mtuci.drivenext.domain.model.FuelType
import com.mtuci.drivenext.domain.model.Transmission
import com.mtuci.drivenext.domain.repository.CarRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.storage.storage

class CarRepositoryImpl(
    private val supabase: SupabaseProvider
) : CarRepository {

    override suspend fun getAvailableCars(): List<Car> = loadCars {}

    override suspend fun searchCars(query: String): List<Car> {
        val words = query.split(' ').map(::sanitizeWord).filter(String::isNotEmpty)
        return loadCars {
            // Каждое слово — в марке ИЛИ в модели; условия разных слов объединяются через И.
            // Поэтому «mercedes sedan» найдёт Mercedes-Benz «S 500 Sedan».
            words.forEach { word ->
                or {
                    ilike("brand", "%$word%")
                    ilike("model", "%$word%")
                }
            }
        }
    }

    override suspend fun getCar(id: String): Car = supabaseCall {
        supabase.client.postgrest[SupabaseProvider.CARS_TABLE]
            .select(Columns.raw(CarDto.COLUMNS)) { filter { eq("id", id) } }
            .decodeSingle<CarDto>()
            .toDomain()
    }

    private suspend fun loadCars(extraFilter: PostgrestFilterBuilder.() -> Unit): List<Car> = supabaseCall {
        supabase.client.postgrest[SupabaseProvider.CARS_TABLE]
            .select(Columns.raw(CarDto.COLUMNS)) {
                filter {
                    eq("is_available", true)
                    extraFilter()
                }
                order("price_per_day", Order.ASCENDING)
            }
            .decodeList<CarDto>()
            .map { it.toDomain() }
    }

    private fun CarDto.toDomain() = Car(
        id = id,
        brand = brand,
        model = model,
        pricePerDay = pricePerDay,
        transmission = if (transmission == "manual") Transmission.MANUAL else Transmission.AUTOMATIC,
        fuelType = when (fuelType) {
            "diesel" -> FuelType.DIESEL
            "electric" -> FuelType.ELECTRIC
            "hybrid" -> FuelType.HYBRID
            else -> FuelType.PETROL
        },
        imageUrl = imagePath?.let(::imageUrl),
        address = address,
        description = description
    )

    /** В image_path можно положить полную ссылку или путь к файлу в публичном бакете cars. */
    private fun imageUrl(path: String): String =
        if (path.startsWith("http")) path else supabase.client.storage.from(SupabaseProvider.CARS_BUCKET).publicUrl(path)

    /**
     * Убираем символы, которые ломают фильтр PostgREST (запятые и скобки разделяют условия в or=(…))
     * или работают как шаблон в LIKE (% и _).
     */
    private fun sanitizeWord(word: String): String = word.filter { it.isLetterOrDigit() || it == '-' || it == '.' }
}
