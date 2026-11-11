package com.kshrd.coreui.components.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.card.AppCard
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.SheetShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

private val InitialsShape = RoundedCornerShape(Radius.tile)

@Composable
private fun InitialsTile(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = InitialsShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** "Website being managed" chip at the top of every portal tab. Tapping opens the switcher sheet. */
@Composable
fun WorkspaceChip(
    website: WorkspaceWebsite?,
    onClick: () -> Unit,
    switchDescription: String,
    modifier: Modifier = Modifier,
) {
    AppCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Space.s, vertical = Space.xs),
        ) {
            InitialsTile(website?.initials.orEmpty())
            Column(modifier = Modifier.weight(1f).padding(horizontal = Space.s)) {
                Text(
                    text = website?.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = website?.domain.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.Filled.UnfoldMore,
                contentDescription = switchDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Bottom sheet that switches the managed website or returns to My Websites.
 * Stateless: selection and navigation are callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteSwitcherSheet(
    websites: List<WorkspaceWebsite>,
    selectedId: String?,
    title: String,
    allWebsitesLabel: String,
    onSelect: (WorkspaceWebsite) -> Unit,
    onAllWebsites: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        shape = SheetShape,
        containerColor = MaterialTheme.wb.canvas,
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = Space.screen).padding(bottom = Space.m),
            verticalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = Space.xxs))
            websites.forEach { site ->
                val selected = site.id == selectedId
                Surface(
                    onClick = { onSelect(site) },
                    shape = MaterialTheme.shapes.large,
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(Space.s),
                    ) {
                        InitialsTile(site.initials)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = Space.s)) {
                            Text(site.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(
                                site.domain,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (selected) Icon(WebIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Surface(onClick = onAllWebsites, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Space.m, vertical = Space.s),
                ) {
                    Icon(WebIcons.Globe, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        allWebsitesLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = Space.s),
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun WorkspaceChipPreview() {
    PreviewSurface {
        WorkspaceChip(
            website = WorkspaceWebsite("1", "Coffee House", "coffeehouse.webblock.com"),
            onClick = {},
            switchDescription = "Switch website",
        )
    }
}
