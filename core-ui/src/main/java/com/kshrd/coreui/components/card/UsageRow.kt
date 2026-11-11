package com.kshrd.coreui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * iOS `UsageDonut` (despite the name a single row): title + "used / limit", an 8dp progress bar on a
 * `wbSoft` track, and a caption. Blue normally, amber from 80%, red at the limit.
 * [leftLabel] / [nearLimitLabel] / [limitReachedLabel] are the three caption formats, passed in so the
 * caller owns the strings: e.g. `{ left -> "$left left" }`.
 */
@Composable
fun UsageRow(
    title: String,
    used: Int,
    limit: Int,
    modifier: Modifier = Modifier,
    leftLabel: (Int) -> String = { "$it left" },
    nearLimitLabel: (Int) -> String = { "$it left · near limit" },
    limitReachedLabel: String = "Limit reached",
) {
    val wb = MaterialTheme.wb
    val progress = if (limit == 0) 0f else minOf(1f, used.toFloat() / limit)
    val left = maxOf(0, limit - used)
    val atLimit = used >= limit
    val near = !atLimit && progress >= 0.8f
    val tint = if (atLimit) wb.red else if (near) wb.amber else wb.brand
    val caption = if (atLimit) limitReachedLabel else if (near) nearLimitLabel(left) else leftLabel(left)
    Column(
        modifier = modifier.fillMaxWidth().semantics { contentDescription = "$title: $used / $limit, $caption" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = wb.text, modifier = Modifier.weight(1f))
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = wb.text, fontWeight = FontWeight.Bold)) { append("$used") }
                        withStyle(SpanStyle(color = wb.muted)) { append(" / $limit") }
                    },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(8.dp).background(wb.soft, PillShape)) {
            Box(Modifier.fillMaxHeight().width(maxOf(8.dp, maxWidth * progress)).background(tint, PillShape))
        }
        Text(caption, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = if (atLimit || near) tint else wb.muted)
    }
}

@ThemePreviews
@Composable
private fun UsageRowPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            UsageRow("Products", used = 12, limit = 30)
            UsageRow("Photos", used = 52, limit = 60)
            UsageRow("Redeploys", used = 5, limit = 5)
        }
    }
}
