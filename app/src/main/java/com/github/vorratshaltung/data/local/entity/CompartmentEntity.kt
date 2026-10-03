package com.github.vorratshaltung.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "compartments",
    foreignKeys = [
        ForeignKey(
            entity = StorageLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["storageLocationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["storageLocationId"])]
)
data class CompartmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storageLocationId: Long,
    val rowIndex: Int,
    val columnIndex: Int,
    val label: String? = null
)
