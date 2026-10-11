package com.github.vorratshaltung.ui.compartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.local.entity.CompartmentEntity
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CompartmentItemUiModel(
    val inventoryItem: InventoryItemEntity,
    val product: ProductEntity
)

data class CompartmentDetailUiState(
    val compartment: CompartmentEntity? = null,
    val items: List<CompartmentItemUiModel> = emptyList(),
    val allProducts: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true
)

class CompartmentDetailViewModel(
    private val compartmentId: Long,
    private val repository: InventoryRepository
) : ViewModel() {

    val uiState: StateFlow<CompartmentDetailUiState> = combine(
        repository.getCompartmentById(compartmentId),
        repository.getItemsForCompartment(compartmentId),
        repository.allProducts
    ) { compartment, items, products ->
        val productMap = products.associateBy { it.id }
        val itemUiModels = items.mapNotNull { item ->
            val product = productMap[item.productId]
            if (product != null) {
                CompartmentItemUiModel(inventoryItem = item, product = product)
            } else null
        }

        CompartmentDetailUiState(
            compartment = compartment,
            items = itemUiModels,
            allProducts = products,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompartmentDetailUiState(isLoading = true)
    )

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

    fun addProductToCompartment(productId: Long, quantity: Int, expirationDate: LocalDate?) {
        viewModelScope.launch {
            val newItem = InventoryItemEntity(
                productId = productId,
                compartmentId = compartmentId,
                quantity = quantity,
                expirationDate = expirationDate
            )
            repository.insertInventoryItem(newItem)
        }
    }

    fun createAndAddProduct(
        name: String,
        targetQty: Int,
        unit: com.github.vorratshaltung.data.model.UnitType,
        unitAmount: Float,
        quantity: Int,
        expirationDate: LocalDate?,
        imageUrl: String?,
        calories: Int? = null,
        protein: Float? = null,
        carbohydrates: Float? = null,
        fat: Float? = null
    ) {
        viewModelScope.launch {
            val newProduct = ProductEntity(
                name = name,
                targetQuantity = targetQty,
                unit = unit,
                unitAmount = unitAmount,
                imageUrl = imageUrl,
                calories = calories,
                protein = protein,
                carbohydrates = carbohydrates,
                fat = fat
            )
            val productId = repository.insertProduct(newProduct)
            val newItem = InventoryItemEntity(
                productId = productId,
                compartmentId = compartmentId,
                quantity = quantity,
                expirationDate = expirationDate
            )
            repository.insertInventoryItem(newItem)
        }
    }
}

class CompartmentDetailViewModelFactory(
    private val compartmentId: Long,
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompartmentDetailViewModel::class.java)) {
            return CompartmentDetailViewModel(compartmentId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
