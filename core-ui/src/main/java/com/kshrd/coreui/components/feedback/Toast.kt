package com.kshrd.coreui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import kotlinx.coroutines.delay

enum class ToastKind { Success, Warning, Error }

/** One toast; a new [id] restarts the 3.5s timer. */
data class ToastData(
    val kind: ToastKind,
    val title: String,
    val message: String,
    val id: Long = System.nanoTime(),
)

/**
 * iOS `.toast(_:)`: a tinted glass banner that slides in from the top, auto-dismisses after 3.5s and
 * dismisses on tap. Place it as the last child of a full-size `Box`.
 */
@Composable
fun WbToastHost(
    toast: ToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(3500)
            dismiss()
        }
    }
    Box(modifier = modifier.fillMaxWidth().statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            // Keep the last value while animating out.
            val shown = remember(toast) { toast }
            if (shown != null) ToastBanner(shown, onClick = onDismiss)
        }
    }
}

@Composable
fun ToastBanner(
    toast: ToastData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val wb = MaterialTheme.wb
    val tint =
        when (toast.kind) {
            ToastKind.Success -> wb.green
            ToastKind.Warning -> wb.amber
            ToastKind.Error -> wb.red
        }
    val icon =
        when (toast.kind) {
            ToastKind.Success -> WebIcons.CircleCheck
            ToastKind.Warning -> WebIcons.Alert
            ToastKind.Error -> WebIcons.Alert
        }
    val shape = RoundedCornerShape(Radius.card)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        modifier =
            modifier
                .padding(horizontal = Space.screen, vertical = Space.xs)
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite }
                .glass(shape, tint.copy(alpha = 0.18f))
                .border(1.dp, tint.copy(alpha = 0.25f), shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(Space.m),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
            Text(toast.title, style = MaterialTheme.typography.titleSmall, color = wb.text)
            Text(toast.message, style = MaterialTheme.typography.bodySmall, color = wb.muted)
        }
    }
}

@ThemePreviews
@Composable
private fun ToastPreview() {
    PreviewSurface {
        Column {
            ToastBanner(ToastData(ToastKind.Success, "Product saved", "Blue lily stem is live."))
            ToastBanner(ToastData(ToastKind.Warning, "Near limit", "You have 2 products left."))
            ToastBanner(ToastData(ToastKind.Error, "Couldn’t redeploy", "Try again in a moment."))
        }
    }
}
