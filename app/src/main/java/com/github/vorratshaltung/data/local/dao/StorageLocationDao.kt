package com.github.vorratshaltung.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageLocationDao {
    @Query("SELECT * FROM storage_locations ORDER BY name ASC")
    fun getAllStorageLocations(): Flow<List<StorageLocationEntity>>

    @Query("SELECT * FROM storage_locations WHERE id = :id")
    fun getStorageLocationById(id: Long): Flow<StorageLocationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorageLocation(storageLocation: StorageLocationEntity): Long

    @Update
    suspend fun updateStorageLocation(storageLocation: StorageLocationEntity): Int

    @Delete
    suspend fun deleteStorageLocation(storageLocation: StorageLocationEntity): Int

    @Query("SELECT * FROM compartments WHERE storageLocationId = :storageLocationId ORDER BY rowIndex ASC, columnIndex ASC")
    fun getCompartmentsForStorage(storageLocationId: Long): Flow<List<CompartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompartments(compartments: List<CompartmentEntity>): List<Long>
}
