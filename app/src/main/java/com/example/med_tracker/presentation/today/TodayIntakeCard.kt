package com.example.med_tracker.presentation.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.domain.model.TodayIntakeItem
import com.example.med_tracker.presentation.common.MedicationFormIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayIntakeCard(
    item: TodayIntakeItem,
    showQuantity: Boolean = true,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(item.scheduledTimeMillis))

    val isMissed = item.status == IntakeStatus.MISSED
    val cardColor = if (isMissed) {
        Color(0xFF5A1E1E)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            1.dp,
            if (isMissed) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MedicationFormIcon(
                    form = item.form,
                    size = 40.dp,
                    iconSize = 22.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val quantityDisplay = if (showQuantity) {
                        val unitStr = if (item.unit.isNotBlank()) " ${item.unit}" else ""
                        " (${item.remainingQuantity}$unitStr)"
                    } else ""

                    val dosageDisplay = if (item.dosage.isNotBlank()) ": ${item.dosage}" else ""
                    Text(
                        text = "${item.form}$dosageDisplay$quantityDisplay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = timeString,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}