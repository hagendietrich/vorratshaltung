package com.github.vorratshaltung.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthNames = listOf(
    "Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
    "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"
)

@Composable
fun MhdPicker(
    selectedDate: LocalDate?,
    onDateChanged: (LocalDate?) -> Unit
) {
    var hasExpiration by remember { mutableStateOf(selectedDate != null) }

    val initialDate = remember { selectedDate ?: LocalDate.now().plusMonths(3) }
    var year by remember { mutableStateOf(initialDate.year) }
    var month by remember { mutableStateOf(initialDate.monthValue) }
    var day by remember { mutableStateOf(initialDate.dayOfMonth) }

    LaunchedEffect(selectedDate) {
        if (selectedDate != null) {
            val currentDate = runCatching { LocalDate.of(year, month, day) }.getOrNull()
            if (currentDate != selectedDate) {
                hasExpiration = true
                year = selectedDate.year
                month = selectedDate.monthValue
                day = selectedDate.dayOfMonth
            }
        }
    }

    val maxDays = remember(year, month) {
        runCatching { YearMonth.of(year, month).lengthOfMonth() }.getOrDefault(31)
    }
    if (day > maxDays) {
        day = maxDays
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Rollen (Jahr, Monat, Tag):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Independent Scrollable Barrel Roll Pickers side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year Picker
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Jahr", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    ScrollableNumberPicker(
                        currentValue = year,
                        range = 2024..2050,
                        onValueChange = { year = it }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Month Picker
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Monat", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    ScrollableMonthPicker(
                        currentMonth = month,
                        onValueChange = { month = it }
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Day Picker
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Tag", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    ScrollableNumberPicker(
                        currentValue = day,
                        range = 1..maxDays,
                        onValueChange = { day = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val previewDate = runCatching { LocalDate.of(year, month, day) }.getOrNull()
            if (previewDate != null) {
                Text(
                    text = "Ausgewählt: ${previewDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    LaunchedEffect(hasExpiration, year, month, day) {
        if (hasExpiration) {
            val date = runCatching { LocalDate.of(year, month, day) }.getOrNull()
            onDateChanged(date)
        } else {
            onDateChanged(null)
        }
    }
}

@Composable
fun ScrollableNumberPicker(
    currentValue: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, currentValue - range.first)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(currentValue) {
        val index = currentValue - range.first
        if (index in 0 until range.count()) {
            if (!listState.isScrollInProgress) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }
            centeredItem?.index
        }.collect { index ->
            if (index != null && index in 0 until range.count()) {
                val value = range.first + index
                if (value != currentValue) {
                    onValueChange(value)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Center highlight box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            )

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = 38.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(range.count()) { index ->
                    val value = range.first + index
                    val isSelected = value == currentValue
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clickable { onValueChange(value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScrollableMonthPicker(
    currentMonth: Int,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, currentMonth - 1)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(currentMonth) {
        val index = currentMonth - 1
        if (index in 0..11) {
            if (!listState.isScrollInProgress) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }
            centeredItem?.index
        }.collect { index ->
            if (index != null && index in 0..11) {
                val monthNum = index + 1
                if (monthNum != currentMonth) {
                    onValueChange(monthNum)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Center highlight box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            )

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = 38.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(12) { index ->
                    val monthNum = index + 1
                    val isSelected = monthNum == currentMonth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clickable { onValueChange(monthNum) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthNames[index],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditExpirationDialog(
    currentDate: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (LocalDate?) -> Unit
) {
    var tempDate by remember { mutableStateOf(currentDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MHD bearbeiten") },
        text = {
            MhdPicker(
                selectedDate = currentDate,
                onDateChanged = { tempDate = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(tempDate)
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
