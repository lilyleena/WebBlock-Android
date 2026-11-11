package com.kshrd.coreui.components.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.R
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.layout.navigationBarBottom
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb

/** One entry of the portal dock. */
data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val BarHeight = 72.dp

/**
 * Portal tabs as a Material 3 navigation bar, styled like the web dashboard's sidebar items: white surface
 * with a hairline top border, muted icons, and the selected tab in brand blue on a soft blue pill.
 * It is drawn over the page, so add [dockContentPadding] to scrolling content.
 */
@Composable
fun AppBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = wb.border)
        NavigationBar(
            containerColor = wb.card,
            tonalElevation = 0.dp,
            modifier = Modifier.height(BarHeight + navigationBarBottom(0.dp)),
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(index) },
                    icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    label = { Text(item.label, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = wb.brand,
                            selectedTextColor = wb.brand,
                            indicatorColor = wb.brandSoft,
                            unselectedIconColor = wb.muted,
                            unselectedTextColor = wb.muted,
                        ),
                )
            }
        }
    }
}

/**
 * Bottom padding for scrolling content on a portal tab: lifts the last item clear of the bottom bar
 * (and the system navigation bar) plus [extra].
 */
@Composable
fun dockContentPadding(extra: Dp = Space.m): Dp = navigationBarBottom(BarHeight + extra)

/** Bottom offset for a floating button that rests just above the bottom bar, [extra] clear of it. */
@Composable
fun dockFloatingOffset(extra: Dp = Space.m): Dp = navigationBarBottom(BarHeight + extra)

private const val MAX_BADGE_COUNT = 9

/**
 * Small red pill with the unread count ("9+" above nine) for the corner of a bell. [ring] is the colour
 * behind the bell, so the badge looks cut out of it. Align it `TopEnd` in the bell's box.
 */
@Composable
fun UnreadBadge(
    count: Int,
    ring: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .offset(x = (-4).dp, y = 6.dp)
                .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                .border(2.dp, ring, CircleShape)
                .padding(Space.xxxs)
                .background(MaterialTheme.wb.red, CircleShape)
                .padding(horizontal = 3.dp),
    ) {
        Text(
            text = if (count > MAX_BADGE_COUNT) stringResource(R.string.wb_badge_overflow) else count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
    }
}

/** Bell with a red dot when there is something unread. */
@Composable
fun NotificationBell(
    hasUnread: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box {
            Icon(WebIcons.Bell, contentDescription = contentDescription, tint = MaterialTheme.wb.text)
            if (hasUnread) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .background(MaterialTheme.wb.red, CircleShape),
                )
            }
        }
    }
}

/** Page title (iOS `PageHeader` title: title2 bold, brand blue) with trailing actions ("Products  + 🔔"). */
@Composable
fun LargeTitleHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = Space.screen, end = Space.xs, top = Space.s, bottom = Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.wb.brand,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) { actions() }
    }
}

/** Pushed-screen bar: "‹ Products   Product   Edit" with the back chevron in brand blue. */
@Composable
fun BackTopBar(
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = Space.xs), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.wb.brand)) {
                Icon(WebIcons.ChevronLeft, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall))
                Text(backLabel, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(start = Space.xxxs))
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { trailing() }
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.wb.text,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 112.dp),
            )
        }
    }
}

/** Portal tab icons (Material stand-ins for the SF Symbols shippingbox / storefront / waveform.path.ecg). */
object PortalIcons {
    val Products = WebIcons.Package
    val ProductsSelected = WebIcons.Package
    val Business = WebIcons.Store
    val BusinessSelected = WebIcons.Store
    val Monitor = WebIcons.Activity
    val MonitorSelected = WebIcons.Activity
}

@ThemePreviews
@Composable
private fun NavComponentsPreview() {
    PreviewSurface(padding = 0) {
        Column {
            Box(Modifier.size(Sizes.compact)) { UnreadBadge(count = 12, ring = Color.White, modifier = Modifier.align(Alignment.TopEnd)) }
            LargeTitleHeader(title = "Products", actions = { NotificationBell(true, {}, "Notifications") })
            BackTopBar(backLabel = "Products", onBack = {}, title = "Product", trailing = {})
            AppBottomBar(
                items =
                    listOf(
                        BottomBarItem("Products", PortalIcons.Products, PortalIcons.ProductsSelected),
                        BottomBarItem("Business", PortalIcons.Business, PortalIcons.BusinessSelected),
                        BottomBarItem("Monitor", PortalIcons.Monitor, PortalIcons.MonitorSelected),
                    ),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }
}
