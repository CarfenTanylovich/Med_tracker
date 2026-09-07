package com.example.med_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.med_tracker.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET remainingQuantity = remainingQuantity - 1 WHERE id = :id AND remainingQuantity > 0")
    suspend fun decrementQuantity(id: Long)

    @Query("UPDATE medications SET remainingQuantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)
}
