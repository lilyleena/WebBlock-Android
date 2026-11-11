package com.kshrd.coreui.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space

private const val DEFAULT_HEIGHT = 16

/** One shimmering placeholder bar. Compose feature skeletons out of these. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Int = DEFAULT_HEIGHT,
) {
    Box(
        modifier =
            modifier
                .height(height.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(ShimmerBrush()),
    )
}

@ThemePreviews
@Composable
private fun SkeletonBoxPreview() {
    PreviewSurface {
        Column {
            SkeletonBox(modifier = Modifier.fillMaxWidth().padding(bottom = Space.xs))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f), height = 24)
        }
    }
}
