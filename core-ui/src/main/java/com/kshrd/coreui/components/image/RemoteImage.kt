package com.kshrd.coreui.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius

/**
 * Loads a photo from [url] (cropped to fill), cross-fading in over a soft placeholder. The
 * placeholder also shows if the image fails to load or the url is empty.
 */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.tile),
) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url.ifBlank { null }).crossfade(true).build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

@ThemePreviews
@Composable
private fun RemoteImagePreview() {
    PreviewSurface { RemoteImage(url = "", contentDescription = null, modifier = Modifier.size(72.dp)) }
}
