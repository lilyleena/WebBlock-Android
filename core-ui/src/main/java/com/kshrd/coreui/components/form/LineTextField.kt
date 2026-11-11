package com.kshrd.coreui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space

/**
 * Quiet "table row" field for forms inside a [com.kshrd.coreui.components.card.FormCard]: small grey
 * label above, the value on a transparent background, and a hairline that turns brand-blue on focus.
 */
@Composable
fun LineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = error != null,
            singleLine = minLines == 1,
            minLines = minLines,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = if (minLines > 1) ImeAction.Default else imeAction),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = Space.xxs))
        }
    }
}

@ThemePreviews
@Composable
private fun LineTextFieldPreview() {
    PreviewSurface {
        Column {
            LineTextField(value = "012 345 678", onValueChange = {}, label = "Phone")
            LineTextField(value = "bad", onValueChange = {}, label = "Email", error = "Enter a valid email")
        }
    }
}
