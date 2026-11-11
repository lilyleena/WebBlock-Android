package com.kshrd.coreui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * The one validation line used under every field, picker and form: a red error icon and footnote text.
 * Long messages wrap beside the icon.
 */
@Composable
fun FieldErrorText(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(WebIcons.Alert, contentDescription = null, tint = MaterialTheme.wb.red, modifier = Modifier.size(14.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.wb.red)
    }
}

@ThemePreviews
@Composable
private fun FieldErrorTextPreview() {
    PreviewSurface { FieldErrorText("Enter your password.") }
}
