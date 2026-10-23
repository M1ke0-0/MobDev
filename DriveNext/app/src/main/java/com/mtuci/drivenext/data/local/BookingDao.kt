package com.mtuci.drivenext.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings ORDER BY startAt DESC")
    fun getAll(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getById(id: Long): BookingEntity?

    // Возвращает id новой строки — это и есть номер бронирования
    @Insert
    suspend fun insert(booking: BookingEntity): Long

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun delete(id: Long)
}
