package com.github.vorratshaltung.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.vorratshaltung.data.local.converters.Converters
import com.github.vorratshaltung.data.local.dao.InventoryItemDao
import com.github.vorratshaltung.data.local.dao.ProductDao
import com.github.vorratshaltung.data.local.dao.StorageLocationDao
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import com.github.vorratshaltung.data.model.StorageType
import com.github.vorratshaltung.data.model.UnitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        StorageLocationEntity::class,
        CompartmentEntity::class,
        ProductEntity::class,
        InventoryItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VorratshaltungDatabase : RoomDatabase() {

    abstract fun storageLocationDao(): StorageLocationDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryItemDao(): InventoryItemDao

    companion object {
        @Volatile
        private var INSTANCE: VorratshaltungDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): VorratshaltungDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VorratshaltungDatabase::class.java,
                    "vorratshaltung_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: VorratshaltungDatabase) {
                val storageDao = db.storageLocationDao()
                val productDao = db.productDao()
                val itemDao = db.inventoryItemDao()

                // Default Storage Location: Kellerregal (6 rows x 3 columns)
                val kellerRegalId = storageDao.insertStorageLocation(
                    StorageLocationEntity(
                        name = "Kellerregal",
                        rows = 6,
                        columns = 3,
                        type = StorageType.SHELF
                    )
                )

                val compartments = mutableListOf<CompartmentEntity>()
                for (r in 0 until 6) {
                    for (c in 0 until 3) {
                        compartments.add(
                            CompartmentEntity(
                                storageLocationId = kellerRegalId,
                                rowIndex = r,
                                columnIndex = c,
                                label = "Fach ${r + 1}-${c + 1}"
                            )
                        )
                    }
                }
                storageDao.insertCompartments(compartments)

                // Sample Products
                val milchId = productDao.insertProduct(
                    ProductEntity(
                        name = "Haltbare Milch 3,5%",
                        targetQuantity = 6,
                        unit = UnitType.PACKUNG,
                        calories = 64,
                        protein = 3.4f,
                        carbohydrates = 4.8f,
                        fat = 3.5f
                    )
                )

                val nudelnId = productDao.insertProduct(
                    ProductEntity(
                        name = "Spaghetti 500g",
                        targetQuantity = 4,
                        unit = UnitType.PACKUNG,
                        calories = 359,
                        protein = 12.0f,
                        carbohydrates = 71.0f,
                        fat = 1.5f
                    )
                )

                val tomatenmarkId = productDao.insertProduct(
                    ProductEntity(
                        name = "Passierte Tomaten 500g",
                        targetQuantity = 8,
                        unit = UnitType.PACKUNG,
                        calories = 24,
                        protein = 1.3f,
                        carbohydrates = 4.0f,
                        fat = 0.2f
                    )
                )

                itemDao.insertItem(
                    InventoryItemEntity(
                        productId = milchId,
                        compartmentId = 1,
                        quantity = 3,
                        expirationDate = LocalDate.now().plusMonths(2)
                    )
                )

                itemDao.insertItem(
                    InventoryItemEntity(
                        productId = nudelnId,
                        compartmentId = 2,
                        quantity = 2,
                        expirationDate = LocalDate.now().plusMonths(6)
                    )
                )

                itemDao.insertItem(
                    InventoryItemEntity(
                        productId = tomatenmarkId,
                        compartmentId = 3,
                        quantity = 4,
                        expirationDate = LocalDate.now().plusMonths(4)
                    )
                )
            }
        }
    }
}
