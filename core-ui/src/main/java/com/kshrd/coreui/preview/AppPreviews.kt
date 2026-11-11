package com.kshrd.coreui.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.theme.AppTheme

/**
 * Multipreview annotation: renders a composable in both light and dark theme.
 *
 * Use this instead of a bare `@Preview` so every component is checked against both colour
 * schemes in the Android Studio preview pane.
 *
 * ```
 * @ThemePreviews
 * @Composable
 * private fun NoteRowPreview() = PreviewSurface { NoteRow(note = sampleNote, onClick = {}) }
 * ```
 */
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/**
 * Multipreview annotation for components whose layout must survive large font settings:
 * text-heavy cards, list rows, form fields.
 */
@Preview(name = "font 100%", showBackground = true, fontScale = 1.0f)
@Preview(name = "font 150%", showBackground = true, fontScale = 1.5f)
annotation class FontScalePreviews

private const val DEFAULT_PREVIEW_PADDING = 16

/**
 * Wraps preview content in [AppTheme] and a themed [Surface] so previews match how the
 * component renders in the app. Always use this inside preview functions.
 */
@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    padding: Int = DEFAULT_PREVIEW_PADDING,
    content: @Composable () -> Unit,
) {
    AppTheme {
        Surface {
            Box(modifier = modifier.padding(padding.dp)) {
                content()
            }
        }
    }
}
