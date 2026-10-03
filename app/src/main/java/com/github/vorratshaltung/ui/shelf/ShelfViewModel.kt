package com.github.vorratshaltung.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import com.github.vorratshaltung.data.model.StorageType
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ShelfViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _selectedLocationId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ShelfUiState> = combine(
        repository.allStorageLocations,
        _selectedLocationId
    ) { locations, selectedId ->
        Pair(locations, selectedId)
    }.flatMapLatest { (locations, selectedId) ->
        if (locations.isEmpty()) {
            flowOf(ShelfUiState(isLoading = false))
        } else {
            val currentLocation = locations.find { it.id == selectedId } ?: locations.first()
            if (_selectedLocationId.value != currentLocation.id) {
                _selectedLocationId.value = currentLocation.id
            }

            combine(
                repository.getCompartmentsForStorage(currentLocation.id),
                repository.getItemsForStorageLocation(currentLocation.id),
                repository.allProducts
            ) { compartments, items, products ->
                val productMap = products.associateBy { it.id }
                val today = LocalDate.now()
                val soonThreshold = today.plusDays(7)

                val gridModels = compartments.map { compartment ->
                    val compartmentItems = items.filter { it.compartmentId == compartment.id }
                    val totalCount = compartmentItems.sumOf { it.quantity }

                    val summary = compartmentItems.mapNotNull { item ->
                        val p = productMap[item.productId]
                        p?.let { "${it.name} (${item.quantity}x)" }
                    }

                    var expired = false
                    var expiringSoon = false

                    for (item in compartmentItems) {
                        item.expirationDate?.let { exp ->
                            if (exp.isBefore(today)) {
                                expired = true
                            } else if (!exp.isAfter(soonThreshold)) {
                                expiringSoon = true
                            }
                        }
                    }

                    CompartmentUiModel(
                        compartment = compartment,
                        totalItemCount = totalCount,
                        itemSummaryList = summary,
                        hasExpiredItems = expired,
                        hasExpiringSoonItems = expiringSoon
                    )
                }

                ShelfUiState(
                    selectedLocation = currentLocation,
                    allLocations = locations,
                    compartmentGrid = gridModels,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShelfUiState(isLoading = true)
    )

    fun selectLocation(locationId: Long) {
        _selectedLocationId.value = locationId
    }

    fun addStorageLocation(name: String, rows: Int, columns: Int, type: StorageType) {
        viewModelScope.launch {
            val newLocation = StorageLocationEntity(
                name = name,
                rows = rows,
                columns = columns,
                type = type
            )
            val compartments = mutableListOf<CompartmentEntity>()
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    compartments.add(
                        CompartmentEntity(
                            storageLocationId = 0,
                            rowIndex = r,
                            columnIndex = c,
                            label = "Fach ${r + 1}-${c + 1}"
                        )
                    )
                }
            }
            val id = repository.insertStorageLocation(newLocation, compartments)
            _selectedLocationId.value = id
        }
    }
}

class ShelfViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShelfViewModel::class.java)) {
            return ShelfViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
