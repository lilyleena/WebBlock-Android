package com.kshrd.coreui.components.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.glassFill
import com.kshrd.coreui.components.menu.WbGlassDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuOption
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/**
 * Read-only field that opens a menu of [options]. Looks like [LabeledField]: title above, glass capsule
 * with a brand outline, error line underneath. [leadingIcon] sits in a small tinted tile at the start
 * ([iconTint] / [iconContainer] recolour it). The menu is the shared pick-one menu, as wide as the field,
 * with the current value checked. An optional [extraActionLabel] adds a trailing "+" action below a divider.
 */
@Composable
fun AppDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    leadingIcon: ImageVector? = null,
    iconTint: Color = MaterialTheme.wb.brand,
    iconContainer: Color = MaterialTheme.wb.brandSoft,
    extraActionLabel: String? = null,
    onExtraAction: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableIntStateOf(0) }
    val wb = MaterialTheme.wb
    val chevron = if (expanded) 180f else 0f
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = wb.text)
        Box {
            Surface(
                onClick = { expanded = true },
                shape = PillShape,
                color = glassFill(),
                border = BorderStroke(if (expanded) 1.5.dp else 1.dp, if (error != null) wb.red else wb.brand.copy(alpha = if (expanded) 1f else 0.3f)),
                modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.control).onSizeChanged { fieldWidth = it.width },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.s),
                    modifier = Modifier.padding(start = if (leadingIcon != null) Space.xs else Space.m, end = Space.m),
                ) {
                    if (leadingIcon != null) {
                        Box(Modifier.size(34.dp).background(iconContainer, PillShape), contentAlignment = Alignment.Center) {
                            Icon(leadingIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(
                        text = value.ifEmpty { placeholder },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (value.isEmpty()) wb.muted.copy(alpha = 0.7f) else wb.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = wb.muted, modifier = Modifier.rotate(chevron))
                }
            }
            WbGlassDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(LocalDensity.current) { fieldWidth.toDp() }).heightIn(max = 320.dp),
            ) {
                options.forEach { option ->
                    WbMenuOption(
                        label = option,
                        selected = option == value,
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
                if (extraActionLabel != null && onExtraAction != null) {
                    if (options.isNotEmpty()) HorizontalDivider(color = wb.border, modifier = Modifier.padding(vertical = Space.xxs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                        modifier =
                            Modifier
                                .padding(horizontal = Space.xs, vertical = Space.xxxs)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.control))
                                .clickable(role = Role.Button) {
                                    expanded = false
                                    onExtraAction()
                                }.padding(horizontal = Space.s, vertical = 10.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.iconSmall))
                        Text(extraActionLabel, style = MaterialTheme.typography.bodyMedium, color = wb.brand)
                    }
                }
            }
        }
        if (error != null) FieldErrorText(error)
    }
}

@ThemePreviews
@Composable
private fun AppDropdownFieldPreview() {
    PreviewSurface {
        AppDropdownField(
            label = "Category",
            value = "Bouquets",
            options = listOf("Flowers", "Bouquets"),
            onSelect = {},
            leadingIcon = Icons.Outlined.Sell,
            iconTint = MaterialTheme.wb.purple,
            iconContainer = MaterialTheme.wb.purpleSoft,
        )
    }
}
