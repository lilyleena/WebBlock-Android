package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

/** The floating "+ Add product" pill that sits above the dock, bottom-right. */
@Composable
fun AddProductButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The shared primary button, hugging its label, with a brand glow because it floats over the list.
    val blue = MaterialTheme.wb.buttonBlue
    PrimaryButton(
        text = stringResource(R.string.pv_add_product),
        onClick = onClick,
        icon = WebIcons.Plus,
        fullWidth = false,
        modifier = modifier.shadow(8.dp, PillShape, ambientColor = blue.copy(alpha = 0.3f), spotColor = blue.copy(alpha = 0.4f)),
    )
}

@ThemePreviews
@Composable
private fun AddProductButtonPreview() {
    PreviewSurface { AddProductButton(onClick = {}, modifier = Modifier.padding(Space.xs)) }
}
