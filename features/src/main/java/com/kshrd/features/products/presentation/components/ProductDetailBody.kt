package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Sell
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.button.SecondaryButton
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.components.card.wbCard
import com.kshrd.coreui.components.menu.WbDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuItem
import com.kshrd.coreui.format.formatPrice
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.products.domain.model.Product

/**
 * Product page toolbar: a round back button on the left and the "⋮" menu (Edit / Delete) on the right,
 * both as glass circles so they read on the page backdrop.
 */
@Composable
fun ProductDetailTopBar(
    productName: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = Space.screen, vertical = Space.xs),
    ) {
        CircleAction(Icons.AutoMirrored.Outlined.ArrowBackIos, stringResource(R.string.pv_detail_back), onBack, iconOffsetStart = true)
        Box(Modifier.weight(1f))
        if (productName != null) {
            Box {
                CircleAction(Icons.Filled.MoreVert, stringResource(R.string.pv_more_actions, productName), { menuOpen = true })
                WbDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    WbMenuItem(Icons.Outlined.Edit, stringResource(R.string.pv_menu_edit), onClick = {
                        menuOpen = false
                        onEdit()
                    })
                    WbMenuItem(Icons.Outlined.DeleteOutline, stringResource(R.string.pv_menu_delete), destructive = true, onClick = {
                        menuOpen = false
                        onDelete()
                    })
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    iconOffsetStart: Boolean = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(Sizes.compact).glass(CircleShape).clickable(role = Role.Button, onClick = onClick),
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = MaterialTheme.wb.text,
            // The iOS-style back chevron sits left of centre in its glyph box; nudge it back to the middle.
            modifier = Modifier.padding(start = if (iconOffsetStart) Space.xxs else 0.dp).size(Sizes.icon),
        )
    }
}

/**
 * Product page body: a large rounded photo (tap the corner button to see it full screen), the name, the
 * price in brand blue, a short description, a row of two facts (category and the website it is shown
 * on), then Delete and Edit buttons.
 */
@Composable
fun ProductDetailBody(
    product: Product,
    websiteName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    var viewerOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.m)) {
        Box {
            ProductPhoto(product.imageUrl, modifier = Modifier.fillMaxWidth().aspectRatio(1f), cornerRadius = Radius.card)
            if (product.imageUrl.isNotBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(Space.xxs)
                            .size(Sizes.compact)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { viewerOpen = true },
                ) {
                    Box(Modifier.size(32.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape))
                    Icon(Icons.Outlined.OpenInFull, stringResource(R.string.pv_photo_expand), tint = Color.White, modifier = Modifier.size(Sizes.iconSmall))
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            Text(product.name, style = MaterialTheme.typography.headlineSmall, color = wb.text)
            Text(formatPrice(product.price), style = MaterialTheme.typography.headlineSmall, color = wb.brand)
            if (product.description.isNotBlank()) {
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = wb.muted,
                )
            }
        }
        FactsRow(category = product.category, websiteName = websiteName)
        // Delete on the left, the main action on the right, as in the product editor.
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.padding(top = Space.xs)) {
            SecondaryButton(
                text = stringResource(R.string.pv_menu_delete),
                onClick = onDelete,
                destructive = true,
                icon = Icons.Outlined.DeleteOutline,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(text = stringResource(R.string.pv_menu_edit), onClick = onEdit, icon = Icons.Outlined.Edit, modifier = Modifier.weight(1f))
        }
    }
    if (viewerOpen) PhotoViewer(product.imageUrl, onClose = { viewerOpen = false })
}

/** Two facts side by side, each in its own small card of equal height. */
@Composable
private fun FactsRow(
    category: String,
    websiteName: String?,
) {
    val wb = MaterialTheme.wb
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        Fact(Icons.Outlined.Sell, wb.purple, wb.purpleSoft, stringResource(R.string.pv_detail_category), category, Modifier.weight(1f))
        if (websiteName != null) {
            Fact(Icons.Outlined.Language, wb.green, wb.greenSoft, stringResource(R.string.pv_detail_shown_on), websiteName, Modifier.weight(1f))
        }
    }
}

/** One fact card: tinted icon tile, then a muted label over the value (two lines max, so long names still fit). */
@Composable
private fun Fact(
    icon: ImageVector,
    tint: Color,
    soft: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Row(
        modifier = modifier.fillMaxHeight().wbCard(padding = Space.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        Box(Modifier.size(40.dp).background(soft, RoundedCornerShape(Radius.tile)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(Sizes.icon))
        }
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxxs), modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = wb.muted, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleSmall, color = wb.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Full-screen photo on black with a close button. */
@Composable
private fun PhotoViewer(
    imageUrl: String,
    onClose: () -> Unit,
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding(), contentAlignment = Alignment.Center) {
            ProductPhoto(imageUrl, modifier = Modifier.fillMaxWidth().aspectRatio(1f), cornerRadius = 0.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(Space.xs)
                        .size(Sizes.compact)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(role = Role.Button, onClick = onClose),
            ) {
                Icon(Icons.Filled.Close, stringResource(R.string.pv_photo_close), tint = Color.White, modifier = Modifier.size(Sizes.icon))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ProductDetailBodyPreview() {
    PreviewSurface {
        Column {
            ProductDetailTopBar(productName = previewProduct.name, onBack = {}, onEdit = {}, onDelete = {})
            ProductDetailBody(previewProduct, websiteName = "Petal & Stem", onEdit = {}, onDelete = {})
        }
    }
}
