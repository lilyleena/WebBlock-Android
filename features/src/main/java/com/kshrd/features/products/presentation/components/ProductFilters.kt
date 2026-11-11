package com.kshrd.features.products.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.ChipRow
import com.kshrd.coreui.components.card.FilterChip
import com.kshrd.coreui.components.card.cardBorder
import com.kshrd.coreui.components.card.cardShadow
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.products.presentation.model.ProductLayout

/**
 * Search capsule on a white card surface (same border and shadow as the cards below it): brand magnifier,
 * the field, a clear button while there is text, and a round filter button that shows or hides the
 * category chips and layout switch. [filtersShown] tints that button.
 */
@Composable
fun ProductSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    filtersShown: Boolean,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.control)
                .cardShadow(PillShape)
                .glass(PillShape)
                .border(cardBorder(), PillShape)
                .padding(start = Space.m, end = Space.xxs),
    ) {
        Icon(WebIcons.Search, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.iconLarge))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.pv_search_placeholder), style = MaterialTheme.typography.bodyLarge, color = wb.muted.copy(alpha = 0.7f), maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = wb.text),
                cursorBrush = SolidColor(wb.brand),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.compact).clip(CircleShape).clickable(role = Role.Button) { onQueryChange("") },
            ) {
                Icon(WebIcons.X, contentDescription = stringResource(R.string.pv_clear_search), tint = wb.muted, modifier = Modifier.size(Sizes.icon))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(Sizes.compact)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onToggleFilters),
        ) {
            // A white disc with a soft shadow, as in the design; the icon dims while the filters are hidden.
            Box(
                Modifier
                    .size(36.dp)
                    .shadow(4.dp, CircleShape, ambientColor = wb.brand.copy(alpha = 0.2f), spotColor = wb.brand.copy(alpha = 0.2f))
                    .background(wb.card, CircleShape),
            )
            Icon(
                WebIcons.Filter,
                contentDescription = stringResource(if (filtersShown) R.string.pv_filters_hide else R.string.pv_filters_show),
                tint = if (filtersShown) wb.brandDeep else wb.muted,
                modifier = Modifier.size(Sizes.icon),
            )
        }
    }
}

/** "All 12" plus one chip per category, in a horizontally scrolling row. */
@Composable
fun ProductCategoryChips(
    categories: List<String>,
    totalCount: Int,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipRow(modifier = modifier) {
        FilterChip(title = stringResource(R.string.pv_chip_all), count = totalCount, selected = selected == null, onClick = { onSelect(null) })
        categories.forEach { name ->
            FilterChip(title = name, selected = selected == name, onClick = { onSelect(name) })
        }
    }
}

/** "Showing 6 of 12 products" with the grid / list switch on the right. */
@Composable
fun ProductSummaryBar(
    summary: String,
    layout: ProductLayout,
    onLayoutChange: (ProductLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.wb.muted)
        LayoutSwitch(layout, onLayoutChange)
    }
}

/** iOS segmented picker with two icons: the selected one sits on a raised card-coloured thumb. */
@Composable
private fun LayoutSwitch(
    layout: ProductLayout,
    onChange: (ProductLayout) -> Unit,
) {
    val wb = MaterialTheme.wb
    Row(
        modifier = Modifier.width(110.dp).height(32.dp).clip(PillShape).background(wb.soft).padding(Space.xxxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(ProductLayout.Grid to R.string.pv_layout_grid, ProductLayout.List to R.string.pv_layout_list).forEach { (target, label) ->
            val selected = target == layout
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(28.dp)
                        .then(
                            if (selected) {
                                Modifier.shadow(1.dp, PillShape).background(wb.card, PillShape).border(BorderStroke(0.5.dp, wb.border), PillShape)
                            } else {
                                Modifier
                            },
                        ).clickable(role = Role.Tab) { onChange(target) },
            ) {
                Icon(
                    imageVector = if (target == ProductLayout.Grid) WebIcons.Grid else WebIcons.List,
                    contentDescription = stringResource(label),
                    tint = if (selected) wb.text else wb.muted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ProductFiltersPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            ProductSearchField(query = "", onQueryChange = {}, filtersShown = true, onToggleFilters = {})
            ProductCategoryChips(listOf("Flowers", "Bouquets", "Gifts", "Events"), totalCount = 12, selected = null, onSelect = {})
            ProductSummaryBar("Showing 6 of 12 products", ProductLayout.Grid, {})
        }
    }
}
