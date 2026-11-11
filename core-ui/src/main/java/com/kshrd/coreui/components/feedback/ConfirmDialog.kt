package com.kshrd.coreui.components.feedback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.wb

/**
 * The app's dialog surface: card colour, card corners, no tonal tint. Every dialog goes through this so
 * confirmations and small forms look the same. [text] is the body (a message, or a field).
 */
@Composable
fun WbAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    val wb = MaterialTheme.wb
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = CardShape,
        containerColor = wb.card,
        titleContentColor = wb.text,
        textContentColor = wb.muted,
        tonalElevation = 0.dp,
    )
}

/** Standard confirm/cancel dialog. Set [destructive] for delete / log out style actions. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    WbAlertDialog(
        title = title,
        onDismissRequest = onDismiss,
        confirmButton = { AppTextAction(text = confirmLabel, onClick = onConfirm, destructive = destructive) },
        dismissButton = { AppTextAction(text = cancelLabel, onClick = onDismiss) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
    )
}

@ThemePreviews
@Composable
private fun ConfirmDialogPreview() {
    PreviewSurface {
        ConfirmDialog(
            title = "Delete product?",
            message = "This removes it from your website.",
            confirmLabel = "Delete",
            cancelLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
            destructive = true,
        )
    }
}
