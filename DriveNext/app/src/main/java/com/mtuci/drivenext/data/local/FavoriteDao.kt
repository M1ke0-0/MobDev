package com.mtuci.drivenext.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    // Flow: Room сам пришлёт новый список после каждой вставки или удаления
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAll(): Flow<List<FavoriteCarEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE carId = :carId)")
    fun isFavorite(carId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: FavoriteCarEntity)

    @Query("DELETE FROM favorites WHERE carId = :carId")
    suspend fun delete(carId: String)
}
