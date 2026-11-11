package com.kshrd.features.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.form.FieldErrorText
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.auth.domain.validation.FieldError

/** Sign-in email message (iOS `AuthViewModel.emailError`). */
@Composable
fun FieldError?.emailMessage(): String? =
    when (this) {
        null -> null
        FieldError.Empty -> stringResource(R.string.auth_error_email_empty)
        else -> stringResource(R.string.auth_error_email_invalid)
    }

/** Sign-in password message (iOS `AuthViewModel.passwordError`). */
@Composable
fun FieldError?.passwordMessage(): String? = if (this == null) null else stringResource(R.string.auth_error_password_empty)

/** Form-level error under the fields: the same red line the fields use, or nothing when [message] is null. */
@Composable
fun ErrorLine(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return
    FieldErrorText(message, modifier = modifier.fillMaxWidth())
}

/** The tinted error box on the sign-in card. */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.wb.redSoft, RoundedCornerShape(Radius.control))
                .padding(Space.s),
    ) {
        ErrorLine(message)
    }
}

/** ──── or ──── */
@Composable
fun OrDivider(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.wb.border))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.wb.muted)
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.wb.border))
    }
}

/** Pulls a view up by [amount] without leaving a gap (SwiftUI's negative padding). */
fun Modifier.pullUp(amount: Dp): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val shift = amount.roundToPx()
        layout(placeable.width, placeable.height - shift) { placeable.placeRelative(0, -shift) }
    }

@ThemePreviews
@Composable
private fun AuthComponentsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            OrDivider("or")
            ErrorBanner("Email or password is incorrect. Check them and try again.")
            ErrorLine("Enter your password.")
        }
    }
}
