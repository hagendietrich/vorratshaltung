package com.github.vorratshaltung.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SortOption {
    MHD, NAME
}

data class InventoryItemUiModel(
    val inventoryItem: InventoryItemEntity,
    val product: ProductEntity,
    val compartment: CompartmentEntity,
    val storageLocation: StorageLocationEntity
)

data class AllInventoryUiState(
    val items: List<InventoryItemUiModel> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.MHD,
    val groupByStorage: Boolean = false,
    val isLoading: Boolean = true
)

class AllInventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.MHD)
    private val _groupByStorage = MutableStateFlow(false)

    val uiState: StateFlow<AllInventoryUiState> = combine(
        combine(repository.allInventoryItems, repository.allProducts, repository.allCompartments) { items, products, compartments ->
            Triple(items, products, compartments)
        },
        combine(repository.allStorageLocations, _searchQuery, _sortOption) { locations, query, sort ->
            Triple(locations, query, sort)
        },
        _groupByStorage
    ) { triple1, triple2, group ->
        val items = triple1.first
        val products = triple1.second
        val compartments = triple1.third
        val locations = triple2.first
        val query = triple2.second
        val sort = triple2.third

        val productMap = products.associateBy { it.id }
        val compartmentMap = compartments.associateBy { it.id }
        val locationMap = locations.associateBy { it.id }

        val uiModels = items.mapNotNull { item ->
            val product = productMap[item.productId]
            val compartment = compartmentMap[item.compartmentId]
            val location = compartment?.let { locationMap[it.storageLocationId] }
            if (product != null && compartment != null && location != null) {
                InventoryItemUiModel(
                    inventoryItem = item,
                    product = product,
                    compartment = compartment,
                    storageLocation = location
                )
            } else null
        }

        val filtered = uiModels.filter {
            it.product.name.contains(query, ignoreCase = true) ||
            it.storageLocation.name.contains(query, ignoreCase = true) ||
            (it.compartment.label?.contains(query, ignoreCase = true) == true)
        }

        val sorted = when (sort) {
            SortOption.NAME -> filtered.sortedBy { it.product.name.lowercase() }
            SortOption.MHD -> filtered.sortedWith(compareBy(nullsLast()) { it.inventoryItem.expirationDate })
        }

        AllInventoryUiState(
            items = sorted,
            searchQuery = query,
            sortOption = sort,
            groupByStorage = group,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AllInventoryUiState(isLoading = true)
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleGroupByStorage() {
        _groupByStorage.value = !_groupByStorage.value
    }

    fun incrementQuantity(itemId: Long) {
        viewModelScope.launch {
            repository.updateItemQuantity(itemId, 1)
        }
    }

    fun decrementQuantity(itemId: Long, currentQuantity: Int) {
        viewModelScope.launch {
            if (currentQuantity > 0) {
                repository.updateItemQuantity(itemId, -1)
            }
        }
    }

    fun deleteItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun updateItemExpiration(item: InventoryItemEntity, newDate: LocalDate?) {
        viewModelScope.launch {
            repository.updateInventoryItem(item.copy(expirationDate = newDate))
        }
    }
}

class AllInventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllInventoryViewModel::class.java)) {
            return AllInventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
