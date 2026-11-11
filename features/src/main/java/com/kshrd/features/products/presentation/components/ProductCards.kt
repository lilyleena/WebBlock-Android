package com.kshrd.features.products.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.kshrd.features.products.domain.model.ProductImages

private const val PRESSED_SCALE = 0.97f

/** iOS `wbCardPress`: the item springs down slightly while touched. */
private fun Modifier.cardPress(onClick: () -> Unit): Modifier =
    composed {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) PRESSED_SCALE else 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
            label = "product-press",
        )
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    }

/**
 * The "⋮" menu on every product card. [onPhoto] draws it as a small white disc so it reads on top of the
 * photo; otherwise it is a bare icon. The touch target is 40dp either way.
 */
@Composable
private fun ProductMenu(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onPhoto: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }
    val wb = MaterialTheme.wb
    Box(modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button, onClick = { open = true }),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    if (onPhoto) {
                        Modifier.size(32.dp).shadow(4.dp, CircleShape).background(wb.card.copy(alpha = 0.95f), CircleShape)
                    } else {
                        Modifier
                    },
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.pv_more_actions, product.name),
                    tint = if (onPhoto) wb.text else wb.muted,
                    modifier = Modifier.size(Sizes.icon),
                )
            }
        }
        WbDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            WbMenuItem(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.pv_menu_edit),
                onClick = {
                    open = false
                    onEdit()
                },
            )
            WbMenuItem(
                icon = Icons.Outlined.DeleteOutline,
                label = stringResource(R.string.pv_menu_delete),
                destructive = true,
                onClick = {
                    open = false
                    onDelete()
                },
            )
        }
    }
}

/** One product per row: 64dp photo, name, "category · description" and price. */
@Composable
fun ProductRow(
    product: Product,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        modifier = modifier.wbCard(padding = Space.s),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier = Modifier.weight(1f).cardPress(onClick),
        ) {
            ProductPhoto(product.imageUrl, modifier = Modifier.size(64.dp))
            Column(verticalArrangement = Arrangement.spacedBy(Space.xxxs), modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = wb.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${product.category} · ${product.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = wb.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(formatPrice(product.price), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = wb.brand)
            }
        }
        ProductMenu(product, onEdit, onDelete)
    }
}

/**
 * Grid card: a square photo filling the top of the card with the "⋮" menu on it, then the name, the
 * price in brand blue and two lines of description. Description always takes two lines so cards in the
 * same row line up. The whole card opens the product.
 */
@Composable
fun ProductTile(
    product: Product,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Column(modifier = modifier.cardPress(onClick).wbCard(padding = 0.dp)) {
        Box {
            ProductPhoto(product.imageUrl, modifier = Modifier.fillMaxWidth().aspectRatio(1f), cornerRadius = Radius.card)
            ProductMenu(product, onEdit, onDelete, onPhoto = true, modifier = Modifier.align(Alignment.TopEnd).padding(Space.xxs))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.xxs),
            modifier = Modifier.padding(start = Space.s, end = Space.s, top = Space.s, bottom = Space.m),
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium,
                color = wb.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(formatPrice(product.price), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = wb.brand)
            Text(
                product.description,
                style = MaterialTheme.typography.bodySmall,
                color = wb.muted,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal val previewProduct =
    Product(
        "p1",
        "Gift bouquet",
        "Bouquets",
        38.0,
        "A hand-tied bouquet of seasonal flowers, wrapped in kraft paper with a ribbon and a small card.",
        ProductImages.drawable("gift_bouquet"),
    )

@ThemePreviews
@Composable
private fun ProductCardsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            ProductTile(previewProduct, onClick = {}, onEdit = {}, onDelete = {}, modifier = Modifier.fillMaxWidth(0.55f))
            ProductRow(previewProduct, onClick = {}, onEdit = {}, onDelete = {})
        }
    }
}
