package com.kshrd.coreui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space

/**
 * Kept for existing call sites: renders the iOS-style [LabeledField] (capsule field, title above).
 * [isPassword] adds the show/hide eye; [minLines] > 1 gives the multi-line variant.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    isPassword: Boolean = false,
    showPasswordLabel: String? = null,
    hidePasswordLabel: String? = null,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    if (showPasswordLabel != null && hidePasswordLabel != null) {
        LabeledField(
            title = label, value = value, onValueChange = onValueChange, modifier = modifier,
            placeholder = placeholder.orEmpty(), error = error, isSecure = isPassword, revealable = isPassword,
            enabled = enabled, minLines = minLines, keyboardType = keyboardType, imeAction = imeAction, onImeAction = onImeAction,
            showPasswordLabel = showPasswordLabel, hidePasswordLabel = hidePasswordLabel,
        )
    } else {
        LabeledField(
            title = label, value = value, onValueChange = onValueChange, modifier = modifier,
            placeholder = placeholder.orEmpty(), error = error, isSecure = isPassword, revealable = isPassword,
            enabled = enabled, minLines = minLines, keyboardType = keyboardType, imeAction = imeAction, onImeAction = onImeAction,
        )
    }
}

@ThemePreviews
@Composable
private fun AppTextFieldPreview() {
    PreviewSurface {
        Column {
            AppTextField(value = "owner@example.com", onValueChange = {}, label = "Email address")
            AppTextField(
                value = "secret",
                onValueChange = {},
                label = "Password",
                isPassword = true,
                error = "Password is required",
                modifier = Modifier.padding(top = Space.s),
            )
        }
    }
}
