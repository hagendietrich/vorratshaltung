package com.github.vorratshaltung.ui.shelf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.vorratshaltung.ui.components.CompactTopBar
import com.github.vorratshaltung.ui.shelf.components.AddStorageDialog
import com.github.vorratshaltung.ui.shelf.components.ShelfGrid

@Composable
fun ShelfScreen(
    viewModel: ShelfViewModel,
    onCompartmentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CompactTopBar(title = "Vorratshaltung")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Neuer Lagerort")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.allLocations, key = { it.id }) { location ->
                        val isSelected = location.id == state.selectedLocation?.id
                        if (isSelected) {
                            Button(
                                onClick = { viewModel.selectLocation(location.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.selectLocation(location.id) }
                            ) {
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                val selectedLocation = state.selectedLocation
                if (selectedLocation != null) {
                    ShelfGrid(
                        location = selectedLocation,
                        compartments = state.compartmentGrid,
                        onCompartmentClick = onCompartmentClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Kein Lagerort vorhanden. Erstelle einen mit +",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStorageDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rows, columns, type ->
                viewModel.addStorageLocation(name, rows, columns, type)
                showAddDialog = false
            }
        )
    }
}
