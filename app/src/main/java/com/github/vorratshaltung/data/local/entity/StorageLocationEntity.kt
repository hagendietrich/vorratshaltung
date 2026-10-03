package com.github.vorratshaltung.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.vorratshaltung.data.model.StorageType

@Entity(tableName = "storage_locations")
data class StorageLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rows: Int,
    val columns: Int,
    val type: StorageType = StorageType.SHELF
)
