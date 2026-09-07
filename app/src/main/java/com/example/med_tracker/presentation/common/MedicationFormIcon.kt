package com.example.med_tracker.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.med_tracker.R

@Composable
fun MedicationFormIcon(
    form: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp
) {
    val (iconRes, iconTint, bgTint) = when (form.lowercase().trim()) {
        "капсула", "капсулы", "капс" -> Triple(
            R.drawable.ic_form_capsule,
            Color(0xFF81C784),
            Color(0xFF1E3320)
        )
        "капли" -> Triple(
            R.drawable.ic_form_drops,
            Color(0xFF64B5F6),
            Color(0xFF192C3E)
        )
        "укол", "инъекция", "ампула" -> Triple(
            R.drawable.ic_form_injection,
            Color(0xFFFFB74D),
            Color(0xFF382A17)
        )
        else -> Triple(
            R.drawable.ic_form_tablet,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .background(bgTint, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = form,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
