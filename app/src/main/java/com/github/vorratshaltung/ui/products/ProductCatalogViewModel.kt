package com.github.vorratshaltung.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.model.UnitType
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductCatalogUiState(
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true
)

class ProductCatalogViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    val uiState: StateFlow<ProductCatalogUiState> = combine(
        repository.allProducts,
        flowOf(false)
    ) { products, _ ->
        ProductCatalogUiState(products = products, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductCatalogUiState(isLoading = true)
    )

    fun saveProduct(
        id: Long,
        name: String,
        targetQuantity: Int,
        unit: UnitType,
        calories: Int?,
        protein: Float?,
        carbohydrates: Float?,
        fat: Float?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.insertProduct(
                    ProductEntity(
                        name = name,
                        targetQuantity = targetQuantity,
                        unit = unit,
                        calories = calories,
                        protein = protein,
                        carbohydrates = carbohydrates,
                        fat = fat,
                        imageUrl = imageUrl
                    )
                )
            } else {
                repository.updateProduct(
                    ProductEntity(
                        id = id,
                        name = name,
                        targetQuantity = targetQuantity,
                        unit = unit,
                        calories = calories,
                        protein = protein,
                        carbohydrates = carbohydrates,
                        fat = fat,
                        imageUrl = imageUrl
                    )
                )
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}

class ProductCatalogViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductCatalogViewModel::class.java)) {
            return ProductCatalogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
