package com.github.vorratshaltung.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.vorratshaltung.data.model.UnitType

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val targetQuantity: Int = 1,
    val unit: UnitType = UnitType.STUECK,
    val calories: Int? = null,
    val protein: Float? = null,
    val carbohydrates: Float? = null,
    val fat: Float? = null
)
