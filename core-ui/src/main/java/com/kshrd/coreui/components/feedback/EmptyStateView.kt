package com.kshrd.coreui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.R
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/** The muted glyph on a soft disc that tops every empty and error state. */
@Composable
private fun StateIcon(icon: ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.wb.muted,
        modifier = Modifier.padding(bottom = Space.xxs).size(56.dp).background(MaterialTheme.wb.soft, CircleShape).padding(14.dp),
    )
}

/**
 * Shared "nothing here" / "something went wrong" view (iOS `ContentUnavailableView`). Used by the `empty`
 * and `error` slots of `DataStateScreenShell`. [icon] sits on a soft disc above the title. Pass
 * [actionLabel] and [onAction] to show the primary pill button.
 */
@Composable
fun EmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Space.l),
        verticalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) StateIcon(icon)
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.wb.text, textAlign = TextAlign.Center)
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.wb.muted,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            PrimaryButton(text = actionLabel, onClick = onAction, fullWidth = false, modifier = Modifier.padding(top = Space.xs))
        }
    }
}

/** iOS `LoadingView`: spinner with a muted caption, at least 240dp tall. */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.wb_loading),
) {
    Column(
        modifier = modifier.fillMaxWidth().heightIn(min = 240.dp),
        verticalArrangement = Arrangement.spacedBy(Space.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = MaterialTheme.wb.brand)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.muted)
    }
}

/** iOS `ErrorStateView`: wifi icon, "Something went wrong", the message and a "Try again" pill. */
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Space.l),
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StateIcon(Icons.Outlined.WifiOff)
        Text(stringResource(R.string.wb_error_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.wb.text, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.wb.muted, textAlign = TextAlign.Center)
        PrimaryButton(text = stringResource(R.string.wb_try_again), onClick = onRetry, fullWidth = false, modifier = Modifier.padding(top = Space.xs))
    }
}

@ThemePreviews
@Composable
private fun EmptyStateViewPreview() {
    PreviewSurface {
        Column {
            EmptyStateView(
                title = "Something went wrong",
                message = "No internet connection",
                icon = Icons.Outlined.WifiOff,
                actionLabel = "Retry",
                onAction = {},
                modifier = Modifier.heightIn(max = 280.dp),
            )
            ErrorStateView(message = "No internet connection", onRetry = {})
            LoadingView(modifier = Modifier.heightIn(max = 120.dp))
        }
    }
}
