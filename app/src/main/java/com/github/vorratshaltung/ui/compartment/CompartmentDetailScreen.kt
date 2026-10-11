package com.github.vorratshaltung.ui.compartment

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.vorratshaltung.data.local.entity.InventoryItemEntity
import com.github.vorratshaltung.ui.compartment.components.AddItemDialog
import com.github.vorratshaltung.ui.components.CompactTopBar
import java.time.LocalDate

@Composable
fun CompartmentDetailScreen(
    viewModel: CompartmentDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItemForMhd by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val compartmentLabel = state.compartment?.let {
        it.label ?: "Fach ${it.rowIndex + 1}-${it.columnIndex + 1}"
    } ?: "Regalfach"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CompactTopBar(
                title = compartmentLabel,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Artikel hinzufügen")
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dieses Fach ist leer. Füge Artikel über + hinzu.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.inventoryItem.id }) { model ->
                    val product = model.product
                    val item = model.inventoryItem

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
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!product.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Einheit: ${product.unit} | Soll: ${product.targetQuantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clickable { editingItemForMhd = item }
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

                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Löschen",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isZero) "Ist-Menge: 0 (Ausgegraut)" else "Ist-Menge: ${item.quantity}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isZero) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.decrementQuantity(item.id, item.quantity) },
                                        enabled = !isZero
                                    ) {
                                        Text("-")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.incrementQuantity(item.id) }
                                    ) {
                                        Text("+")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            allProducts = state.allProducts,
            onDismiss = { showAddDialog = false },
            onAddExisting = { productId, qty, date ->
                viewModel.addProductToCompartment(productId, qty, date)
            },
            onAddNewProduct = { name, targetQty, unit, qty, date, imgUrl, cal, prot, carbs, fat ->
                viewModel.createAndAddProduct(name, targetQty, unit, qty, date, imgUrl, cal, prot, carbs, fat)
            }
        )
    }

    val itemToEdit = editingItemForMhd
    if (itemToEdit != null) {
        EditExpirationDialog(
            currentDate = itemToEdit.expirationDate,
            onDismiss = { editingItemForMhd = null },
            onSave = { newDate ->
                viewModel.updateItemExpiration(itemToEdit, newDate)
            }
        )
    }
}

@Composable
fun EditExpirationDialog(
    currentDate: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (LocalDate?) -> Unit
) {
    var dateText by remember { mutableStateOf(currentDate?.toString() ?: LocalDate.now().plusMonths(3).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MHD bearbeiten") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("MHD (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedDate = runCatching { LocalDate.parse(dateText) }.getOrNull()
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
