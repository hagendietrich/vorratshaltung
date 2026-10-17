package com.github.vorratshaltung.ui.statistics

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.vorratshaltung.data.model.UnitType
import com.github.vorratshaltung.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val totalItemsCount: Int = 0,
    val uniqueProductsCount: Int = 0,
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val personCount: Int = 1,
    val isLoading: Boolean = false,
)

class StatisticsViewModel(
    application: Application,
    repository: InventoryRepository,
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("vorratshaltung_prefs", Context.MODE_PRIVATE)
    private val _personCount = MutableStateFlow(sharedPreferences.getInt("person_count", 1))

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.allProducts,
        repository.allInventoryItems,
        _personCount,
    ) { products, items, persons ->
        val productMap = products.associateBy { it.id }

        var totalQty = 0
        val activeProductIds = mutableSetOf<Long>()
        var caloriesSum = 0f
        var proteinSum = 0f
        var carbsSum = 0f
        var fatSum = 0f

        val itemQtyMap = items.groupingBy { it.productId }.fold(0) { acc, item -> acc + item.quantity }

        itemQtyMap.forEach { (productId, qty) ->
            if (qty > 0) {
                val product = productMap[productId]
                if (product != null) {
                    totalQty += qty
                    activeProductIds.add(productId)

                    val totalAmount = qty * product.unitAmount
                    val factor = when (product.unit) {
                        UnitType.GRAMM -> totalAmount / 100f
                        UnitType.MILLILITER -> totalAmount / 100f
                        UnitType.LITER -> (totalAmount * 1000f) / 100f
                        UnitType.PACKUNG -> totalAmount / 100f
                        UnitType.STUECK -> {
                            if (product.unitAmount <= 5f) qty * product.unitAmount
                            else totalAmount / 100f
                        }
                    }

                    caloriesSum += factor * (product.calories ?: 0)
                    proteinSum += factor * (product.protein ?: 0f)
                    carbsSum += factor * (product.carbohydrates ?: 0f)
                    fatSum += factor * (product.fat ?: 0f)
                }
            }
        }

        StatisticsUiState(
            totalItemsCount = totalQty,
            uniqueProductsCount = activeProductIds.size,
            totalCalories = caloriesSum,
            totalProtein = proteinSum,
            totalCarbs = carbsSum,
            totalFat = fatSum,
            personCount = persons,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState(isLoading = true),
    )

    fun setPersonCount(count: Int) {
        val validCount = count.coerceAtLeast(1)
        _personCount.value = validCount
        sharedPreferences.edit {
            putInt("person_count", validCount)
        }
    }
}

class StatisticsViewModelFactory(
    private val application: Application,
    private val repository: InventoryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
