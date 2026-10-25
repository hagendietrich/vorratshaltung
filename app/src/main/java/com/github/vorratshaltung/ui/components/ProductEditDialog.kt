package com.github.vorratshaltung.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.vorratshaltung.data.local.entity.ProductEntity
import com.github.vorratshaltung.data.model.UnitType
import com.github.vorratshaltung.ui.products.ProductImageSearchDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, targetQty: Int, unit: UnitType, unitAmount: Float, calories: Int?, protein: Float?, carbohydrates: Float?, fat: Float?, imageUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var targetQtyText by remember { mutableStateOf(product?.targetQuantity?.toString() ?: "1") }
    var unitAmountText by remember { mutableStateOf(product?.unitAmount?.toString() ?: "1") }
    var selectedUnit by remember { mutableStateOf(product?.unit ?: UnitType.STUECK) }
    var expandedUnit by remember { mutableStateOf(false) }

    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }

    var calText by remember { mutableStateOf(product?.calories?.toString() ?: "") }
    var protText by remember { mutableStateOf(product?.protein?.toString() ?: "") }
    var carbsText by remember { mutableStateOf(product?.carbohydrates?.toString() ?: "") }
    var fatText by remember { mutableStateOf(product?.fat?.toString() ?: "") }

    var showImageSearchDialog by remember { mutableStateOf(false) }

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
        title = { Text(if (product == null) "Neues Produkt" else "Produkt bearbeiten") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
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
                    value = targetQtyText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) targetQtyText = it },
                    label = { Text("Soll-Anzahl (Einheiten)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit }
                ) {
                    OutlinedTextField(
                        value = selectedUnit.displayLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Einheit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        UnitType.values().forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayLabel) },
                                onClick = {
                                    selectedUnit = unit
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitAmountText,
                    onValueChange = { unitAmountText = it },
                    label = { Text("Einheiten pro Packung") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calText,
                        onValueChange = { calText = it },
                        label = { Text("kcal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protText,
                        onValueChange = { protText = it },
                        label = { Text("Eiweiß (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Kohlenh. (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Fett (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val tQty = targetQtyText.toIntOrNull() ?: 1
                    val uAmount = unitAmountText.toFloatOrNull() ?: 1f
                    val cal = calText.toIntOrNull()
                    val prot = protText.toFloatOrNull()
                    val carbs = carbsText.toFloatOrNull()
                    val fat = fatText.toFloatOrNull()

                    if (name.isNotBlank()) {
                        onSave(
                            product?.id ?: 0L,
                            name,
                            tQty,
                            selectedUnit,
                            uAmount,
                            cal,
                            prot,
                            carbs,
                            fat,
                            imageUrl.ifBlank { null }
                        )
                    }
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
            initialQuery = if (name.isNotBlank()) name else "food",
            onDismiss = { showImageSearchDialog = false },
            onImageSelected = { prod ->
                prod.imageUrl?.let { imageUrl = it }
                if (name.isBlank()) name = prod.name
                if (calText.isBlank() && prod.calories != null) calText = prod.calories.toString()
                if (protText.isBlank() && prod.protein != null) protText = prod.protein.toString()
                if (carbsText.isBlank() && prod.carbohydrates != null) carbsText = prod.carbohydrates.toString()
                if (fatText.isBlank() && prod.fat != null) fatText = prod.fat.toString()
                showImageSearchDialog = false
            }
        )
    }
}
