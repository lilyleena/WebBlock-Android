package com.kshrd.coreui.components.card

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

enum class BadgeTone { Success, Danger, Info, Neutral }

/** Small status pill ("Live", "Draft", "SUCCEEDED"). Colours come from the theme, never raw. */
@Composable
fun StatusBadge(
    text: String,
    tone: BadgeTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val wb = MaterialTheme.wb
    val (container: Color, content: Color) =
        when (tone) {
            BadgeTone.Success -> wb.greenSoft to wb.green
            BadgeTone.Danger -> wb.redSoft to wb.red
            BadgeTone.Info -> wb.brandSoft to wb.brand
            BadgeTone.Neutral -> wb.soft to wb.muted
        }
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = Space.xxs),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(Modifier.size(4.dp))
            }
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@ThemePreviews
@Composable
private fun StatusBadgePreview() {
    PreviewSurface {
        Row {
            StatusBadge("Live", BadgeTone.Success)
            StatusBadge("Draft", BadgeTone.Neutral, modifier = Modifier.padding(start = Space.xs))
            StatusBadge("Offline", BadgeTone.Danger, modifier = Modifier.padding(start = Space.xs))
        }
    }
}
