package com.github.vorratshaltung.ui.compartment.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.github.vorratshaltung.ui.components.MhdPicker
import com.github.vorratshaltung.ui.components.ImageCropDialog
import java.io.File
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.font.FontWeight
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
    onAddNewProduct: (name: String, targetQty: Int, unit: UnitType, unitAmount: Float, quantity: Int, expirationDate: LocalDate?, imageUrl: String?, calories: Int?, protein: Float?, carbohydrates: Float?, fat: Float?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productSearchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(allProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) allProducts
        else allProducts.filter { it.name.contains(productSearchQuery, ignoreCase = true) }
    }

    var newName by remember { mutableStateOf("") }
    var newTargetQtyText by remember { mutableStateOf("1") }
    var unitAmountText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UnitType.STUECK) }
    var expandedUnitDropdown by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf<Int?>(null) }
    var protein by remember { mutableStateOf<Float?>(null) }
    var carbs by remember { mutableStateOf<Float?>(null) }
    var fat by remember { mutableStateOf<Float?>(null) }

    var showImageSearchDialog by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var expirationDate by remember { mutableStateOf<LocalDate?>(LocalDate.now().plusMonths(3)) }

    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var imageToCropUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                imageToCropUri = uri
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageToCropUri = uri
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
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        label = { Text("Produkt suchen...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedProduct != null) {
                        Text(
                            text = "Ausgewählt: ${selectedProduct!!.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Keine Produkte gefunden", color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredProducts, key = { it.id }) { product ->
                                    val isSelected = selectedProduct?.id == product.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedProduct = product },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = product.name,
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
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
                            Text("Galerie")
                        }

                        OutlinedButton(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kamera")
                        }

                        OutlinedButton(
                            onClick = { showImageSearchDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suchen")
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
                            value = selectedUnit.displayLabel,
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
                                    text = { Text(unit.displayLabel) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = unitAmountText,
                        onValueChange = { unitAmountText = it },
                        label = { Text("Menge pro Packung") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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

                MhdPicker(
                    selectedDate = expirationDate,
                    onDateChanged = { expirationDate = it }
                )
            }
        },
        confirmButton = {
            val canConfirm = selectedTab == 1 || selectedProduct != null
            TextButton(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    val date = expirationDate

                    if (selectedTab == 0) {
                        selectedProduct?.let { p ->
                            onAddExisting(p.id, qty, date)
                        }
                    } else {
                        val targetQty = newTargetQtyText.toIntOrNull() ?: 1
                        val uAmount = unitAmountText.toFloatOrNull() ?: 1f
                        if (newName.isNotBlank()) {
                            onAddNewProduct(newName, targetQty, selectedUnit, uAmount, qty, date, imageUrl.ifBlank { null }, calories, protein, carbs, fat)
                        }
                    }
                    onDismiss()
                },
                enabled = canConfirm
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

    if (imageToCropUri != null) {
        ImageCropDialog(
            imageUri = imageToCropUri!!,
            onDismiss = { imageToCropUri = null },
            onCropped = { croppedUri ->
                imageUrl = croppedUri.toString()
                imageToCropUri = null
            }
        )
    }

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
