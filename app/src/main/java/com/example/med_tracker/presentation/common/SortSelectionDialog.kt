package com.example.med_tracker.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.med_tracker.presentation.cabinet.MedicationSortOrder

@Composable
fun SortSelectionDialog(
    currentSort: MedicationSortOrder,
    onDismiss: () -> Unit,
    onSelect: (MedicationSortOrder) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка лекарств") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MedicationSortOrder.entries.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(order) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == order,
                            onClick = { onSelect(order) }
                        )
                        Text(
                            text = order.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
