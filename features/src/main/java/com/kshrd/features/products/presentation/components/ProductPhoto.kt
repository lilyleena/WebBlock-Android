package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.image.RemoteImage
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.wb
import com.kshrd.features.products.domain.model.ProductImages

/**
 * iOS `ProductPhoto`: the product's photo cropped to fill, on a soft brand tile. A bundled sample
 * (`drawable:<name>`), a picked photo (uri), or the leaf placeholder when there is none.
 */
@Composable
fun ProductPhoto(
    imageUrl: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.tile,
    shape: Shape = RoundedCornerShape(cornerRadius),
) {
    val context = LocalContext.current
    val resId =
        remember(imageUrl) {
            if (imageUrl.startsWith(ProductImages.DRAWABLE_PREFIX)) {
                context.resources.getIdentifier(imageUrl.removePrefix(ProductImages.DRAWABLE_PREFIX), "drawable", context.packageName)
            } else {
                0
            }
        }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clip(shape).background(MaterialTheme.wb.brandSoft),
    ) {
        when {
            resId != 0 -> Image(painterResource(resId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            imageUrl.isNotBlank() && !imageUrl.startsWith(ProductImages.DRAWABLE_PREFIX) ->
                RemoteImage(url = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp))
            else -> Icon(Icons.Outlined.Eco, contentDescription = null, tint = MaterialTheme.wb.brand, modifier = Modifier.size(28.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun ProductPhotoPreview() {
    PreviewSurface { ProductPhoto(imageUrl = "", modifier = Modifier.size(64.dp)) }
}
