package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.components.form.FieldErrorText
import com.kshrd.coreui.components.menu.WbDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuItem
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.products.domain.model.ProductImages

private val FieldShape = RoundedCornerShape(Radius.panel)

/** Width : height of the photo and the "Change photo" box beside it. */
private const val TILE_RATIO = 1.15f

/**
 * The editor's photo row. With a photo: the photo on the left and a dashed "Change photo" box on the
 * right, the same size. Long-press the photo for "Remove photo". Without one: a single wide dashed "Add a
 * photo" box with the size hint. A spinner replaces the camera while a picked photo uploads.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductPhotoField(
    imageUrl: String,
    isUploading: Boolean,
    error: String?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val hasPhoto = imageUrl.isNotBlank()
    Column(verticalArrangement = Arrangement.spacedBy(Space.xxs), modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.fillMaxWidth()) {
            if (hasPhoto) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .aspectRatio(TILE_RATIO)
                            .clip(FieldShape)
                            .combinedClickable(role = Role.Button, onClick = onPick, onLongClick = { menuOpen = true }),
                ) {
                    ProductPhoto(imageUrl, modifier = Modifier.fillMaxSize(), cornerRadius = Radius.panel)
                    WbDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        WbMenuItem(
                            icon = Icons.Outlined.DeleteOutline,
                            label = stringResource(R.string.pv_photo_remove),
                            destructive = true,
                            onClick = {
                                menuOpen = false
                                onRemove()
                            },
                        )
                    }
                }
            }
            DropZone(
                label = stringResource(if (hasPhoto) R.string.pv_photo_change else R.string.pv_photo_add),
                hint = if (hasPhoto) null else stringResource(R.string.pv_photo_hint),
                isUploading = isUploading,
                onClick = onPick,
                modifier = Modifier.weight(1f).then(if (hasPhoto) Modifier.aspectRatio(TILE_RATIO) else Modifier.height(150.dp)),
            )
        }
        if (error != null) FieldErrorText(error)
    }
}

/** Dashed glass box with a brand camera and a label: tap to open the photo picker. */
@Composable
private fun DropZone(
    label: String,
    hint: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val dash = wb.brand.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterVertically),
        modifier =
            modifier
                .clip(FieldShape)
                .background(glassFill(wb.brandSoft.copy(alpha = 0.35f)))
                .drawBehind {
                    drawRoundRect(
                        color = dash,
                        cornerRadius = CornerRadius(Radius.panel.toPx()),
                        style = Stroke(width = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                    )
                }.clickable(role = Role.Button, enabled = !isUploading, onClick = onClick),
    ) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            if (isUploading) {
                CircularProgressIndicator(color = wb.brand, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = wb.brand, modifier = Modifier.size(28.dp))
            }
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = wb.brand, textAlign = TextAlign.Center)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.labelMedium, color = wb.muted, textAlign = TextAlign.Center)
        }
    }
}

@ThemePreviews
@Composable
private fun ProductPhotoFieldPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            ProductPhotoField(imageUrl = "", isUploading = false, error = null, onPick = {}, onRemove = {})
            ProductPhotoField(imageUrl = ProductImages.drawable("gift_bouquet"), isUploading = false, error = null, onPick = {}, onRemove = {})
        }
    }
}
