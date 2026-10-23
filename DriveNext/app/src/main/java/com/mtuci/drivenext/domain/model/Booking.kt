package com.mtuci.drivenext.domain.model

import java.time.Duration
import java.time.LocalDateTime

/** Бронирование автомобиля (хранится локально в Room). */
data class Booking(
    val id: Long,
    val car: Car,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val driverName: String,
    val licenseNumber: String
) {
    val price: BookingPrice
        get() = BookingPrice.calculate(car.pricePerDay, start, end)

    /** Аренда ещё не закончилась — бронирование активное, его можно открыть и отменить. */
    fun isActive(now: LocalDateTime = LocalDateTime.now()): Boolean = end.isAfter(now)
}

/** Стоимость аренды за весь период. */
data class BookingPrice(
    val days: Int,
    val rent: Int,
    val insurance: Int
) {
    val total: Int
        get() = rent + insurance

    val deposit: Int
        get() = DEPOSIT

    companion object {
        const val INSURANCE_PER_DAY = 300
        const val DEPOSIT = 15000

        fun calculate(pricePerDay: Int, start: LocalDateTime, end: LocalDateTime): BookingPrice {
            // Минимум сутки: аренда с 27 по 27 число считается за один день
            val days = maxOf(1, Duration.between(start, end).toDays().toInt())
            return BookingPrice(days = days, rent = pricePerDay * days, insurance = INSURANCE_PER_DAY * days)
        }
    }
}
