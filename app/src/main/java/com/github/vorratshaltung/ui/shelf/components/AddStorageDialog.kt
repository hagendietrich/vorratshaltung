package com.github.vorratshaltung.ui.shelf.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.vorratshaltung.data.model.StorageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStorageDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, rows: Int, columns: Int, type: StorageType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rowsText by remember { mutableStateOf("3") }
    var colsText by remember { mutableStateOf("3") }
    var selectedType by remember { mutableStateOf(StorageType.SHELF) }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuen Lagerort anlegen") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bezeichnung (z.B. Vorratskammer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = when (selectedType) {
                            StorageType.SHELF -> "Regal"
                            StorageType.DRAWER -> "Schublade"
                            StorageType.PANTRY -> "Vorratskammer"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lagertyp") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Regal") },
                            onClick = {
                                selectedType = StorageType.SHELF
                                expandedTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Schublade") },
                            onClick = {
                                selectedType = StorageType.DRAWER
                                expandedTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Vorratskammer") },
                            onClick = {
                                selectedType = StorageType.PANTRY
                                expandedTypeDropdown = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rowsText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) rowsText = it },
                    label = { Text("Anzahl Reihen (übereinander)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = colsText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) colsText = it },
                    label = { Text("Anzahl Spalten (nebeneinander)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rows = rowsText.toIntOrNull() ?: 1
                    val cols = colsText.toIntOrNull() ?: 1
                    if (name.isNotBlank() && rows > 0 && cols > 0) {
                        onConfirm(name, rows, cols, selectedType)
                    }
                }
            ) {
                Text("Anlegen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
