package com.github.vorratshaltung.ui.compartment.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.model.UnitType
import com.github.vorratshaltung.ui.products.ProductImageSearchDialog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    allProducts: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddExisting: (productId: Long, quantity: Int, expirationDate: LocalDate?) -> Unit,
    onAddNewProduct: (name: String, targetQty: Int, unit: UnitType, quantity: Int, expirationDate: LocalDate?, imageUrl: String?, calories: Int?, protein: Float?, carbohydrates: Float?, fat: Float?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var selectedProduct by remember { mutableStateOf<ProductEntity?>(allProducts.firstOrNull()) }
    var expandedProductDropdown by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newTargetQtyText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UnitType.STUECK) }
    var expandedUnitDropdown by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf<Int?>(null) }
    var protein by remember { mutableStateOf<Float?>(null) }
    var carbs by remember { mutableStateOf<Float?>(null) }
    var fat by remember { mutableStateOf<Float?>(null) }

    var showImageSearchDialog by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var dateText by remember { mutableStateOf(LocalDate.now().plusMonths(3).toString()) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Artikel zum Fach hinzufügen") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Aus Katalog") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Neu anlegen") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    ExposedDropdownMenuBox(
                        expanded = expandedProductDropdown,
                        onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "Bitte Produkt wählen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Produkt") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedProductDropdown,
                            onDismissRequest = { expandedProductDropdown = false }
                        ) {
                            allProducts.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text(product.name) },
                                    onClick = {
                                        selectedProduct = product
                                        expandedProductDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Produktname") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Bild-URL") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        if (!imageUrl.isBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aus Galerie")
                        }

                        OutlinedButton(
                            onClick = { showImageSearchDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bild suchen")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTargetQtyText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) newTargetQtyText = it },
                        label = { Text("Soll-Anzahl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedUnitDropdown,
                        onExpandedChange = { expandedUnitDropdown = !expandedUnitDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Einheit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedUnitDropdown,
                            onDismissRequest = { expandedUnitDropdown = false }
                        ) {
                            UnitType.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                    label = { Text("Anzahl (Ist)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    val qty = quantityText.toIntOrNull() ?: 1
                    val date = runCatching { LocalDate.parse(dateText) }.getOrNull()

                    if (selectedTab == 0) {
                        selectedProduct?.let { p ->
                            onAddExisting(p.id, qty, date)
                        }
                    } else {
                        val targetQty = newTargetQtyText.toIntOrNull() ?: 1
                        if (newName.isNotBlank()) {
                            onAddNewProduct(newName, targetQty, selectedUnit, qty, date, imageUrl.ifBlank { null }, calories, protein, carbs, fat)
                        }
                    }
                    onDismiss()
                }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )

    if (showImageSearchDialog) {
        ProductImageSearchDialog(
            initialQuery = if (newName.isNotBlank()) newName else "food",
            onDismiss = { showImageSearchDialog = false },
            onImageSelected = { prod ->
                prod.imageUrl?.let { imageUrl = it }
                if (newName.isBlank()) newName = prod.name
                if (calories == null) calories = prod.calories
                if (protein == null) protein = prod.protein
                if (carbs == null) carbs = prod.carbohydrates
                if (fat == null) fat = prod.fat
                showImageSearchDialog = false
            }
        )
    }
}
