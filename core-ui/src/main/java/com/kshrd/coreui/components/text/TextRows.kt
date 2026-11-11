package com.kshrd.coreui.components.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * iOS `SectionTitle`: subheadline semibold, muted. The one heading used above a group of cards on every
 * screen. [count] adds a small brand capsule after the title ("New  3").
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val wb = MaterialTheme.wb
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Space.xxs).semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = wb.muted)
        if (count != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = wb.brand,
                modifier = Modifier.clip(PillShape).background(wb.brandSoft).padding(horizontal = Space.xs, vertical = 1.dp),
            )
        }
    }
}

/**
 * A titled group: [SectionTitle] sitting close to its [content], so the title reads as belonging to the
 * card below it and not to the one above. Put it in a column spaced by `Space.m`.
 */
@Composable
fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        SectionTitle(title)
        content()
    }
}

/** iOS `InfoRow`: muted label on the left, value on the right. */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.muted)
        Spacer(Modifier.weight(1f).padding(start = Space.m))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.text, textAlign = TextAlign.End)
    }
}

/** iOS `DetailField`: small caption over a value, as in the Figma "Business details" card. */
@Composable
fun DetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isLink: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = MaterialTheme.wb.muted)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isLink) MaterialTheme.wb.brand else MaterialTheme.wb.text,
        )
    }
}

/** iOS `PageHeader`: bold blue title (title2) with a muted subtitle. */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().semantics { heading() }, verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.wb.brand)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.muted)
    }
}

@ThemePreviews
@Composable
private fun TextRowsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.padding(Space.xxs)) {
            PageHeader("Products", "Manage what you sell")
            SectionTitle("Business details", count = 3)
            InfoRow("Plan", "Pro plan")
            DetailField("Website", "petalandstem.webblock.com", isLink = true)
        }
    }
}
