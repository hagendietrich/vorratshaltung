package com.github.vorratshaltung.data.repository

import com.github.vorratshaltung.data.local.dao.InventoryItemDao
import com.github.vorratshaltung.data.local.dao.ProductDao
import com.github.vorratshaltung.data.local.dao.StorageLocationDao
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class InventoryRepository(
    private val storageLocationDao: StorageLocationDao,
    private val productDao: ProductDao,
    private val inventoryItemDao: InventoryItemDao
) {
    val allStorageLocations: Flow<List<StorageLocationEntity>> = storageLocationDao.getAllStorageLocations()

    fun getStorageLocation(id: Long): Flow<StorageLocationEntity?> = storageLocationDao.getStorageLocationById(id)

    fun getCompartmentsForStorage(storageLocationId: Long): Flow<List<CompartmentEntity>> =
        storageLocationDao.getCompartmentsForStorage(storageLocationId)

    suspend fun insertStorageLocation(location: StorageLocationEntity, compartments: List<CompartmentEntity>): Long {
        val locationId = storageLocationDao.insertStorageLocation(location)
        val updatedCompartments = compartments.map { it.copy(storageLocationId = locationId) }
        storageLocationDao.insertCompartments(updatedCompartments)
        return locationId
    }

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProduct(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    fun getItemsForCompartment(compartmentId: Long): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsForCompartment(compartmentId)

    fun getItemsForStorageLocation(storageLocationId: Long): Flow<List<InventoryItemEntity>> =
        inventoryItemDao.getItemsForStorageLocation(storageLocationId)

    suspend fun insertInventoryItem(item: InventoryItemEntity): Long = inventoryItemDao.insertItem(item)

    suspend fun updateInventoryItem(item: InventoryItemEntity) = inventoryItemDao.updateItem(item)

    suspend fun deleteInventoryItem(item: InventoryItemEntity) = inventoryItemDao.deleteItem(item)

    suspend fun updateItemQuantity(itemId: Long, delta: Int) = inventoryItemDao.updateQuantity(itemId, delta)

    fun getEarliestExpirationForProduct(productId: Long): Flow<LocalDate?> =
        inventoryItemDao.getEarliestExpirationForProduct(productId)
}
