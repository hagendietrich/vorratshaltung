package com.github.vorratshaltung.ui.shelf

import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity

data class CompartmentUiModel(
    val compartment: CompartmentEntity,
    val totalItemCount: Int = 0,
    val itemSummaryList: List<String> = emptyList(),
    val hasExpiredItems: Boolean = false,
    val hasExpiringSoonItems: Boolean = false
)

data class ShelfUiState(
    val selectedLocation: StorageLocationEntity? = null,
    val allLocations: List<StorageLocationEntity> = emptyList(),
    val compartmentGrid: List<CompartmentUiModel> = emptyList(),
    val isLoading: Boolean = true
)
