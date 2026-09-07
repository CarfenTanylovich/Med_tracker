package com.example.med_tracker.presentation.cabinet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.presentation.common.MedicationFormIcon

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MedicationFormIcon(
                    form = medication.form,
                    size = 46.dp,
                    iconSize = 24.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val subText = buildString {
                        append(medication.form)
                        if (medication.dosage.isNotBlank()) append(" • ${medication.dosage}")
                    }
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val unitDisplay = if (medication.unit.isNotBlank()) " ${medication.unit}" else ""
                    Text(
                        text = "Остаток: ${medication.remainingQuantity}$unitDisplay",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (medication.remainingQuantity <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
