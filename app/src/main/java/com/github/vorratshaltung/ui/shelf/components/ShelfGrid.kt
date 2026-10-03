package com.github.vorratshaltung.ui.shelf.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.vorratshaltung.data.local.entity.StorageLocationEntity
import com.github.vorratshaltung.ui.shelf.CompartmentUiModel

@Composable
fun ShelfGrid(
    location: StorageLocationEntity,
    compartments: List<CompartmentUiModel>,
    onCompartmentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        for (r in 0 until location.rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until location.columns) {
                    val model = compartments.find { 
                        it.compartment.rowIndex == r && it.compartment.columnIndex == c 
                    }

                    if (model != null) {
                        CompartmentCard(
                            model = model,
                            onClick = { onCompartmentClick(model.compartment.id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}
