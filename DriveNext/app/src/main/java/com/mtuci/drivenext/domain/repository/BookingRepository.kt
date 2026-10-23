package com.mtuci.drivenext.domain.repository

import com.mtuci.drivenext.domain.model.Booking
import kotlinx.coroutines.flow.Flow

/** Бронирования хранятся на устройстве (Room). */
interface BookingRepository {
    fun getBookings(): Flow<List<Booking>>

    suspend fun getBooking(id: Long): Booking?

    /** Возвращает номер нового бронирования. */
    suspend fun create(booking: Booking): Long

    suspend fun cancel(id: Long)
}
