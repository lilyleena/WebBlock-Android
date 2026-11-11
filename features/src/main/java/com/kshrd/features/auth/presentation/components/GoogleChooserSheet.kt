package com.kshrd.features.auth.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.AvatarView
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.SheetShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

/** iOS `GoogleChooserSheet`: a half-height sheet with the one sample Google account. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GoogleChooserSheet(
    isLoading: Boolean,
    errorMessage: String?,
    onAccountClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.wb.canvas,
        shape = SheetShape,
    ) {
        GoogleChooserContent(isLoading, errorMessage, onAccountClick, onDismiss)
    }
}

@Composable
fun GoogleChooserContent(
    isLoading: Boolean,
    errorMessage: String?,
    onAccountClick: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Space.screen).padding(bottom = Space.xl),
        verticalArrangement = Arrangement.spacedBy(Space.m),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.auth_google_cancel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.wb.brand,
                modifier = Modifier.align(Alignment.CenterStart).clip(PillShape).clickable(role = Role.Button, onClick = onCancel).padding(vertical = Space.xs),
            )
            Text(
                text = stringResource(R.string.auth_google_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.wb.text,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stringResource(R.string.auth_google_continue_to),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.wb.muted,
        )
        AccountRow(isLoading = isLoading, onClick = onAccountClick)
        ErrorLine(errorMessage)
    }
}

@Composable
private fun AccountRow(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val wb = MaterialTheme.wb
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = PillShape,
        color = wb.card,
        border = BorderStroke(1.5.dp, wb.brand.copy(alpha = 0.45f)),
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(6.dp, PillShape, ambientColor = wb.brand.copy(alpha = 0.15f), spotColor = wb.brand.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.l, vertical = Space.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            AvatarView(initials = stringResource(R.string.auth_google_initials), size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.auth_google_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = wb.text,
                )
                Text(stringResource(R.string.auth_google_email), style = MaterialTheme.typography.bodySmall, color = wb.muted)
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(Sizes.icon), strokeWidth = 2.dp, color = wb.brand)
            } else {
                Icon(WebIcons.ChevronRight, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.icon))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun GoogleChooserPreview() {
    PreviewSurface {
        GoogleChooserContent(isLoading = false, errorMessage = null, onAccountClick = {}, onCancel = {})
    }
}
