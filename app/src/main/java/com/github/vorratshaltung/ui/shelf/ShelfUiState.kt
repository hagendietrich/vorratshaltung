package com.github.vorratshaltung.ui.shelf

import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity

data class CompartmentProductUiModel(
    val productId: Long,
    val productName: String,
    val imageUrl: String?,
    val quantity: Int
)

data class CompartmentUiModel(
    val compartment: CompartmentEntity,
    val totalItemCount: Int = 0,
    val products: List<CompartmentProductUiModel> = emptyList(),
    val hasExpiredItems: Boolean = false,
    val hasExpiringSoonItems: Boolean = false
)

data class ShelfUiState(
    val selectedLocation: StorageLocationEntity? = null,
    val allLocations: List<StorageLocationEntity> = emptyList(),
    val compartmentGrid: List<CompartmentUiModel> = emptyList(),
    val isLoading: Boolean = true
)
