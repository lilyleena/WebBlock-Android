package com.kshrd.coreui.icons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.wb

/** App-specific line icons that Material Icons doesn't offer. Draw with `Icon(...)` so the tint applies. */
object WbIcons {
    /** Rounded open box with an arrow leaving to the right. */
    val LogOut: ImageVector by lazy {
        lineIcon(
            name = "WbLogOut",
            "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
            "M16 17l5-5-5-5",
            "M21 12H9",
        )
    }
}

/** 24dp outline icon: each path is stroked 2dp wide with round caps and joins, like the screenshot set. */
private fun lineIcon(
    name: String,
    vararg paths: String,
): ImageVector =
    ImageVector
        .Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply {
            paths.forEach { data ->
                addPath(
                    pathData = PathParser().parsePathString(data).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

@ThemePreviews
@Composable
private fun WbIconsPreview() {
    PreviewSurface {
        Row {
            Icon(WebIcons.LogOut, contentDescription = null, tint = MaterialTheme.wb.red, modifier = Modifier.size(24.dp))
        }
    }
}
