package com.github.vorratshaltung.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.model.formatAmount
import com.github.vorratshaltung.ui.components.CompactTopBar
import com.github.vorratshaltung.ui.components.ProductDetailDialog
import java.time.LocalDate

@Composable
fun AllInventoryScreen(
    viewModel: AllInventoryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var editingItemForMhd by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var viewingProductDetails by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CompactTopBar(title = "Bestand")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Produkt oder Vorrat suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp)
            )

            // Sorting & Grouping Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = state.sortOption == SortOption.MHD,
                        onClick = { viewModel.setSortOption(SortOption.MHD) },
                        label = { Text("MHD") }
                    )
                    FilterChip(
                        selected = state.sortOption == SortOption.NAME,
                        onClick = { viewModel.setSortOption(SortOption.NAME) },
                        label = { Text("Name") }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Nach Vorrat",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = state.groupByStorage,
                        onCheckedChange = { viewModel.toggleGroupByStorage() }
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isBlank()) "Keine Artikel im Bestand vorhanden." else "Keine Artikel gefunden.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.groupByStorage) {
                        val grouped = state.items.groupBy { it.storageLocation }
                        grouped.forEach { (location, locationItems) ->
                            item(key = "header_${location.id}") {
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            items(locationItems, key = { it.inventoryItem.id }) { model ->
                                InventoryItemCard(
                                    model = model,
                                    onIncrement = { viewModel.incrementQuantity(model.inventoryItem.id) },
                                    onDecrement = { viewModel.decrementQuantity(model.inventoryItem.id, model.inventoryItem.quantity) },
                                    onDelete = { viewModel.deleteItem(model.inventoryItem) },
                                    onEditMhd = { editingItemForMhd = model.inventoryItem },
                                    onImageClick = { viewingProductDetails = model.product },
                                    showStorageInfo = false
                                )
                            }
                        }
                    } else {
                        items(state.items, key = { it.inventoryItem.id }) { model ->
                            InventoryItemCard(
                                model = model,
                                onIncrement = { viewModel.incrementQuantity(model.inventoryItem.id) },
                                onDecrement = { viewModel.decrementQuantity(model.inventoryItem.id, model.inventoryItem.quantity) },
                                onDelete = { viewModel.deleteItem(model.inventoryItem) },
                                onEditMhd = { editingItemForMhd = model.inventoryItem },
                                onImageClick = { viewingProductDetails = model.product },
                                showStorageInfo = true
                            )
                        }
                    }
                }
            }
        }
    }

    val itemToEdit = editingItemForMhd
    if (itemToEdit != null) {
        AllEditExpirationDialog(
            currentDate = itemToEdit.expirationDate,
            onDismiss = { editingItemForMhd = null },
            onSave = { newDate ->
                viewModel.updateItemExpiration(itemToEdit, newDate)
            }
        )
    }

    val productToView = viewingProductDetails
    if (productToView != null) {
        ProductDetailDialog(
            product = productToView,
            onDismiss = { viewingProductDetails = null }
        )
    }
}

@Composable
fun InventoryItemCard(
    model: InventoryItemUiModel,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onEditMhd: () -> Unit,
    onImageClick: () -> Unit,
    showStorageInfo: Boolean
) {
    val product = model.product
    val item = model.inventoryItem
    val compartment = model.compartment
    val location = model.storageLocation

    val isZero = item.quantity == 0
    val cardAlpha = if (isZero) 0.55f else 1f

    val today = LocalDate.now()
    val mhdStatusColor = when {
        item.expirationDate == null -> MaterialTheme.colorScheme.outlineVariant
        item.expirationDate.isBefore(today) -> Color(0xFFE53935)
        item.expirationDate.isBefore(today.plusDays(7)) -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(
            containerColor = if (isZero) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageModifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onImageClick() }

                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = imageModifier
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = imageModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (showStorageInfo) {
                        val compartmentLabel = compartment.label ?: "Fach ${compartment.rowIndex + 1}-${compartment.columnIndex + 1}"
                        Text(
                            text = "${location.name} > $compartmentLabel",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Einheit: ${product.unit.formatAmount(product.unitAmount)} | Soll: ${product.targetQuantity} Einheiten",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { onEditMhd() }
                    ) {
                        Text(
                            text = "MHD: ${item.expirationDate ?: "Nicht angegeben"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = mhdStatusColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "MHD bearbeiten",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZero) "Ist-Menge: 0" else "Ist-Menge: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isZero) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDecrement,
                        enabled = !isZero
                    ) {
                        Text("-")
                    }
                    OutlinedButton(
                        onClick = onIncrement
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}

@Composable
fun AllEditExpirationDialog(
    currentDate: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (LocalDate?) -> Unit
) {
    var hasExpiration by remember { mutableStateOf(currentDate != null) }
    var dateText by remember { mutableStateOf(currentDate?.toString() ?: LocalDate.now().plusMonths(3).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MHD bearbeiten") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasExpiration = !hasExpiration },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = hasExpiration,
                        onCheckedChange = { hasExpiration = it }
                    )
                    Text(text = "Mit MHD (Mindesthaltbarkeitsdatum)")
                }

                if (hasExpiration) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("MHD (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedDate = if (hasExpiration) runCatching { LocalDate.parse(dateText) }.getOrNull() else null
                    onSave(parsedDate)
                    onDismiss()
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
