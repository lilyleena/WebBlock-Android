package com.kshrd.coreui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/** iOS `Pill`: capsule with a soft tinted background; [uppercase] gives the bold caption2 status look. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.wb.brand,
    background: Color = MaterialTheme.wb.brandSoft,
    uppercase: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        modifier = modifier.clip(PillShape).glass(PillShape, background.copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = Space.xxs),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        Text(
            text = if (uppercase) text.uppercase() else text,
            style = if (uppercase) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelMedium,
            color = tint,
            maxLines = 1,
        )
    }
}

/** SUCCEEDED / FAILED pill used in the deploy lists. */
@Composable
fun ResultPill(
    succeeded: Boolean,
    succeededLabel: String,
    failedLabel: String,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Pill(
        text = if (succeeded) succeededLabel else failedLabel,
        modifier = modifier,
        tint = if (succeeded) wb.green else wb.red,
        background = if (succeeded) wb.greenSoft else wb.redSoft,
    )
}

@ThemePreviews
@Composable
private fun PillsPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
            Pill("Live", icon = Icons.Filled.CheckCircle)
            ResultPill(succeeded = true, succeededLabel = "Success", failedLabel = "Failed")
            ResultPill(succeeded = false, succeededLabel = "Success", failedLabel = "Failed")
        }
    }
}
