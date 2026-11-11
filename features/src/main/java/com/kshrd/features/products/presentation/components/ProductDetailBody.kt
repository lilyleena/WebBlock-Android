package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.button.SecondaryButton
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.components.card.wbCard
import com.kshrd.coreui.components.menu.WbDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuItem
import com.kshrd.coreui.format.formatPrice
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.overline
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
 * Product page body: the photo in a white frame with a "Live on website" pill (tap it to see it full
 * screen), the name with its category chip, the price (large dollars, small sign and cents), an "About
 * this product" card, and a "Shown on" card that opens the website. Edit and Delete sit in
 * [ProductDetailActions], pinned under this.
 */
@Composable
fun ProductDetailBody(
    product: Product,
    websiteName: String?,
    onOpenWebsite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    var viewerOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.m)) {
        FramedPhoto(product.imageUrl, live = websiteName != null, onOpen = { viewerOpen = true })
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxs), modifier = Modifier.padding(top = Space.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall, color = wb.text, modifier = Modifier.weight(1f))
                Pill(product.category, icon = Icons.Filled.Sell)
            }
            PriceText(product.price)
        }
        if (product.description.isNotBlank()) {
            Column(modifier = Modifier.wbCard(), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                Text(stringResource(R.string.pv_detail_about).uppercase(), style = MaterialTheme.typography.overline, color = wb.muted)
                Text(product.description, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp), color = wb.text)
            }
        }
        if (websiteName != null) ShownOnCard(websiteName, onOpenWebsite)
    }
    if (viewerOpen) PhotoViewer(product.imageUrl, onClose = { viewerOpen = false })
}

/** The photo inside a thick white frame with a soft shadow; the "Live on website" pill sits on its bottom-left. */
@Composable
private fun FramedPhoto(
    imageUrl: String,
    live: Boolean,
    onOpen: () -> Unit,
) {
    val wb = MaterialTheme.wb
    val frame = RoundedCornerShape(Radius.card)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(10.dp, frame, ambientColor = wb.brand.copy(alpha = 0.18f), spotColor = wb.brand.copy(alpha = 0.18f))
                .background(wb.card, frame)
                .padding(Space.xxs),
    ) {
        val photo = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(Radius.card - Space.xxs))
        ProductPhoto(
            imageUrl,
            modifier = if (imageUrl.isNotBlank()) photo.clickable(role = Role.Button, onClickLabel = stringResource(R.string.pv_photo_expand), onClick = onOpen) else photo,
            cornerRadius = Radius.card - Space.xxs,
        )
        if (live) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xxs + Space.xxxs),
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(Space.s)
                        .background(wb.card.copy(alpha = 0.92f), PillShape)
                        .padding(horizontal = Space.s, vertical = Space.xxs + Space.xxxs),
            ) {
                Box(Modifier.size(12.dp).border(2.dp, wb.green.copy(alpha = 0.3f), CircleShape).padding(2.dp).background(wb.green, CircleShape))
                Text(stringResource(R.string.pv_detail_live), style = MaterialTheme.typography.labelLarge, color = wb.green)
            }
        }
    }
}

/** "$6.00" as a small raised sign, large bold dollars and small raised cents, in brand blue. */
@Composable
private fun PriceText(price: Double) {
    val wb = MaterialTheme.wb
    val text = formatPrice(price)
    val firstDigit = text.indexOfFirst { it.isDigit() }.coerceAtLeast(0)
    val decimal = text.lastIndexOf('.').takeIf { it > firstDigit } ?: text.length
    val small = SpanStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize, baselineShift = BaselineShift(0.4f))
    Text(
        text =
            buildAnnotatedString {
                withStyle(small) { append(text.substring(0, firstDigit)) }
                append(text.substring(firstDigit, decimal))
                withStyle(small) { append(text.substring(decimal)) }
            },
        style = MaterialTheme.typography.displaySmall,
        color = wb.brand,
    )
}

/** Card with the website the product is shown on; tapping it opens the website. */
@Composable
private fun ShownOnCard(
    websiteName: String,
    onOpen: () -> Unit,
) {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        modifier = Modifier.clip(CardShape).clickable(role = Role.Button, onClick = onOpen).wbCard(),
    ) {
        Box(Modifier.size(40.dp).background(wb.greenSoft, RoundedCornerShape(Radius.tile)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Public, contentDescription = null, tint = wb.green, modifier = Modifier.size(Sizes.icon))
        }
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxxs), modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.pv_detail_shown_on), style = MaterialTheme.typography.labelMedium, color = wb.muted)
            Text(websiteName, style = MaterialTheme.typography.titleSmall, color = wb.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.icon))
    }
}

/** Delete and Edit, pinned to the bottom of the product page on a page-coloured bar with a hairline on top. */
@Composable
fun ProductDetailActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Column(modifier = modifier.fillMaxWidth().background(wb.card)) {
        HorizontalDivider(color = wb.border)
        Row(
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier = Modifier.navigationBarsPadding().padding(horizontal = Space.screen, vertical = Space.s),
        ) {
            SecondaryButton(
                text = stringResource(R.string.pv_detail_delete),
                onClick = onDelete,
                destructive = true,
                icon = Icons.Filled.Delete,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(text = stringResource(R.string.pv_menu_edit), onClick = onEdit, icon = Icons.Filled.Edit, modifier = Modifier.weight(1.5f))
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
            ProductDetailBody(previewProduct, websiteName = "Petal & Stem", onOpenWebsite = {})
            ProductDetailActions(onEdit = {}, onDelete = {})
        }
    }
}
