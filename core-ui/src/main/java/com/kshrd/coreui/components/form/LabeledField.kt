package com.kshrd.coreui.components.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.R
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.FieldShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

private val MultilineShape = FieldShape

/**
 * Web dashboard input: semibold title (+ "optional"), rounded-rect field with a hairline grey outline that turns
 * brand 1.5dp on focus and red with an error row on error. [minLines] > 1 gives the rounded-rect multi-line
 * variant (`Radius.panel`). [prefix] puts fixed text before the value, such as a currency sign. [readOnly] uses the soft fill and a fainter outline. [hint] shows under the field.
 */
@Composable
fun LabeledField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    hint: String? = null,
    isOptional: Boolean = false,
    isSecure: Boolean = false,
    revealable: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    showPasswordLabel: String = stringResource(R.string.wb_show_password),
    hidePasswordLabel: String = stringResource(R.string.wb_hide_password),
    prefix: String? = null,
) {
    val wb = MaterialTheme.wb
    val multiline = minLines > 1
    val shape = if (multiline) MultilineShape else FieldShape
    var focused by remember { mutableStateOf(false) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    val outline =
        when {
            error != null -> wb.red
            focused -> wb.brand
            else -> wb.border
        }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xxs), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = wb.text)
            if (isOptional) Text(stringResource(R.string.wb_optional), style = MaterialTheme.typography.labelMedium, color = wb.muted)
        }
        Row(
            verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Sizes.control)
                    .clip(shape)
                    .background(if (readOnly) wb.soft.copy(alpha = 0.9f) else glassFill())
                    .border(if (focused || error != null) 1.5.dp else 1.dp, outline, shape)
                    .padding(horizontal = if (multiline) Space.s else Space.m, vertical = if (multiline) Space.xs else 0.dp),
        ) {
            if (prefix != null && !multiline) {
                Text(prefix, style = MaterialTheme.typography.bodyMedium, color = wb.muted, modifier = Modifier.padding(end = Space.xs))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled && !readOnly,
                readOnly = readOnly,
                singleLine = !multiline,
                minLines = minLines,
                maxLines = if (multiline) 8 else 1,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = if (readOnly) wb.muted else wb.text),
                cursorBrush = SolidColor(wb.brand),
                visualTransformation = if (isSecure && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = if (isSecure) KeyboardType.Password else keyboardType,
                        capitalization = if (isSecure || keyboardType == KeyboardType.Email) KeyboardCapitalization.None else capitalization,
                        autoCorrectEnabled = !(isSecure || keyboardType == KeyboardType.Email),
                        imeAction = if (multiline) ImeAction.Default else imeAction,
                    ),
                keyboardActions = KeyboardActions(onAny = { onImeAction?.invoke() }),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).onFocusChanged { focused = it.isFocused },
                decorationBox = { inner ->
                    Box(contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart, modifier = Modifier.heightIn(min = 48.dp)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = wb.muted.copy(alpha = 0.7f))
                        }
                        inner()
                    }
                },
            )
            if (isSecure && revealable) {
                Icon(
                    imageVector = if (revealed) WebIcons.EyeOff else WebIcons.Eye,
                    contentDescription = if (revealed) hidePasswordLabel else showPasswordLabel,
                    tint = wb.muted,
                    modifier =
                        Modifier
                            .padding(start = Space.xs)
                            .size(Sizes.iconLarge)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button) { revealed = !revealed },
                )
            }
        }
        if (error != null) {
            FieldErrorText(error)
        } else if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = wb.muted)
        }
    }
}

@ThemePreviews
@Composable
private fun LabeledFieldPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            LabeledField("Email", "", {}, placeholder = "you@example.com", keyboardType = KeyboardType.Email)
            LabeledField("Password", "secret", {}, isSecure = true, revealable = true, error = "Enter your password.")
            LabeledField("Description", "Hand-tied bouquets", {}, minLines = 3, isOptional = true, hint = "Shown on your website.")
            LabeledField("Website", "petalandstem.webblock.com", {}, readOnly = true)
        }
    }
}
