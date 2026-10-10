package com.github.vorratshaltung.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ShoppingItemUiModel(
    val product: ProductEntity,
    val currentQuantity: Int,
    val missingQuantity: Int
)

data class ShoppingListUiState(
    val shoppingItems: List<ShoppingItemUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class ShoppingListViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    val uiState: StateFlow<ShoppingListUiState> = combine(
        repository.allProducts,
        repository.allInventoryItems
    ) { products, items ->
        val itemQtyMap = items.groupingBy { it.productId }.fold(0) { acc, item -> acc + item.quantity }

        val shoppingItems = products.mapNotNull { product ->
            val currentQty = itemQtyMap[product.id] ?: 0
            val missing = product.targetQuantity - currentQty
            if (missing > 0) {
                ShoppingItemUiModel(
                    product = product,
                    currentQuantity = currentQty,
                    missingQuantity = missing
                )
            } else null
        }

        ShoppingListUiState(
            shoppingItems = shoppingItems,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShoppingListUiState(isLoading = true)
    )
}

class ShoppingListViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            return ShoppingListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
