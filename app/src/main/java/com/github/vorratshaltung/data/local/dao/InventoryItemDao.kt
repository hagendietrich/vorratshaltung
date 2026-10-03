package com.github.vorratshaltung.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items WHERE compartmentId = :compartmentId")
    fun getItemsForCompartment(compartmentId: Long): Flow<List<InventoryItemEntity>>

    @Query(
        """
        SELECT inventory_items.* FROM inventory_items 
        INNER JOIN compartments ON inventory_items.compartmentId = compartments.id 
        WHERE compartments.storageLocationId = :storageLocationId
        """
    )
    fun getItemsForStorageLocation(storageLocationId: Long): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity): Int

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity): Int

    @Query("UPDATE inventory_items SET quantity = quantity + :delta WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Long, delta: Int): Int

    @Query("SELECT MIN(expirationDate) FROM inventory_items WHERE productId = :productId AND expirationDate IS NOT NULL")
    fun getEarliestExpirationForProduct(productId: Long): Flow<LocalDate?>
}
