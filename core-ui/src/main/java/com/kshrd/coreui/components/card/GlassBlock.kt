package com.kshrd.coreui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space

/**
 * A section of a detail page ("About", "Contact"): the same flat web dashboard card as [WbCard]
 * (solid surface, hairline border, soft blue shadow), with spaced content.
 */
@Composable
fun GlassBlock(
    modifier: Modifier = Modifier,
    spacing: Dp = Space.m,
    contentPadding: Dp = Space.m,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.wbCard(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@ThemePreviews
@Composable
private fun GlassBlockPreview() {
    PreviewSurface {
        GlassBlock { Text("Rounded gradient glass") }
    }
}
