package com.kshrd.coreui.components.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.R
import com.kshrd.coreui.layout.navigationBarBottom
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** One entry of the portal dock. */
data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

/**
 * Port of iOS `DockShape`: a pill with a round cutout in its top edge at [notchCenter] (px), so the circular
 * button sits snugly inside it. [notchRadius] and [fillet] are in px.
 */
class DockShape(
    private val notchCenter: Float,
    private val notchRadius: Float,
    private val fillet: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = size.width
        val h = size.height
        val r = h / 2
        val rad = notchRadius
        val f = fillet
        val cx = notchCenter.coerceIn(r + rad + f, w - r - rad - f)
        val theta = 20.0 * PI / 180
        val leftX = cx - rad * cos(theta).toFloat()
        val leftY = rad * sin(theta).toFloat()
        val path =
            Path().apply {
                moveTo(r, 0f)
                lineTo(cx - rad - f, 0f)
                quadraticBezierTo(cx - rad, 0f, leftX, leftY)
                // 160° -> 20° through the bottom of the circle (y grows downward).
                arcTo(Rect(Offset(cx, 0f), rad), 160f, -140f, false)
                quadraticBezierTo(cx + rad, 0f, cx + rad + f, 0f)
                lineTo(w - r, 0f)
                arcTo(Rect(Offset(w - r, h / 2), r), -90f, 180f, false)
                lineTo(r, h)
                arcTo(Rect(Offset(r, h / 2), r), 90f, 180f, false)
                close()
            }
        return Outline.Generic(path)
    }
}

private val BarHeight = 64.dp
private val Lift = 28.dp
private val SideInset = 30.dp
private val NotchRadius = 35.dp
private val Fillet = 12.dp
private val ButtonSize = 54.dp

/**
 * iOS `GlassDock`: a floating notched pill with the portal tabs. The selected tab rises into a round
 * blue button sitting in the notch, which slides to the next tab with a spring. The page behind the
 * dock stays visible around it, so add bottom padding to scrolling content.
 */
@Composable
fun AppBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Space.m).height(BarHeight + Lift),
    ) {
        val sideInset = SideInset
        val tabWidth = (maxWidth - sideInset * 2) / items.size
        val targetX = with(androidx.compose.ui.platform.LocalDensity.current) { (sideInset + tabWidth * (selectedIndex + 0.5f)).toPx() }
        val notchX by animateFloatAsState(targetX, spring(dampingRatio = 0.78f, stiffness = 180f), label = "dock-notch")
        val density = androidx.compose.ui.platform.LocalDensity.current
        val shape = DockShape(notchX, with(density) { NotchRadius.toPx() }, with(density) { Fillet.toPx() })
        // The light rim is a glass highlight in light mode; in dark mode it is toned down so the dock is not outlined in white.
        val rimStrength = if (wb.isDark) 0.2f else 1f
        val rim =
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.95f * rimStrength), Color.White.copy(alpha = 0.25f * rimStrength), Color.White.copy(alpha = 0.8f * rimStrength)),
            )
        val glass = Brush.verticalGradient(listOf(wb.card.copy(alpha = 0.92f), wb.brandSoft.copy(alpha = if (wb.isDark) 0.92f else 0.88f)))

        Row(
            modifier =
                Modifier
                    .offset(y = Lift)
                    .fillMaxWidth()
                    .height(BarHeight)
                    .shadow(10.dp, shape, ambientColor = wb.brand.copy(alpha = 0.10f), spotColor = wb.brand.copy(alpha = 0.10f))
                    .clip(shape)
                    // Gradient glass (no real blur: backdrop blur stalled scrolling). Mostly opaque so the
                    // page underneath only faintly shows through and never fights with the labels.
                    .background(glass, shape)
                    .border(1.2.dp, rim, shape)
                    .border(1.dp, wb.brand.copy(alpha = 0.22f), shape)
                    .padding(horizontal = sideInset),
        ) {
            items.forEachIndexed { index, item ->
                DockTab(item = item, selected = index == selectedIndex, onClick = { onSelect(index) }, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }

        // Floating round button showing the selected tab.
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .offset { IntOffset((notchX - ButtonSize.toPx() / 2).roundToInt(), (Lift - ButtonSize / 2).roundToPx()) }
                    .size(ButtonSize)
                    .shadow(6.dp, CircleShape, ambientColor = wb.brand.copy(alpha = 0.28f), spotColor = wb.brand.copy(alpha = 0.28f))
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF5A8AE0), wb.buttonBlue)), CircleShape)
                    .border(2.5.dp, if (wb.isDark) wb.card else Color.White, CircleShape),
        ) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = { (scaleIn(initialScale = 0.5f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.5f) + fadeOut()) },
                label = "dock-icon",
            ) { index ->
                Icon(items[index].selectedIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun DockTab(
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            modifier
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                )
                .semantics { contentDescription = item.label },
    ) {
        // The selected icon lives in the round button, so it is hidden here but keeps its space.
        Icon(
            item.icon,
            contentDescription = null,
            tint = wb.text,
            modifier = Modifier.height(24.dp).size(22.dp).alpha(if (selected) 0f else 1f),
        )
        Text(
            item.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) wb.brand else wb.text,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/**
 * Bottom padding for scrolling content on a portal tab: lifts the last item clear of the floating dock
 * (bar, raised button and the system navigation bar) plus [extra].
 */
@Composable
fun dockContentPadding(extra: Dp = Space.m): Dp = navigationBarBottom(BarHeight + Lift + extra)

/** Bottom offset for a floating button that rests just above the dock's bar, [extra] clear of it. */
@Composable
fun dockFloatingOffset(extra: Dp = Space.s): Dp = navigationBarBottom(BarHeight + extra)

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
            Icon(Icons.Outlined.Notifications, contentDescription = contentDescription, tint = MaterialTheme.wb.text)
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
                Icon(Icons.AutoMirrored.Outlined.ArrowBackIos, contentDescription = null, modifier = Modifier.size(Sizes.iconSmall))
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
    val Products = Icons.Rounded.ShoppingBag
    val ProductsSelected = Icons.Rounded.ShoppingBag
    val Business = Icons.Rounded.Storefront
    val BusinessSelected = Icons.Rounded.Storefront
    val Monitor = Icons.Rounded.Insights
    val MonitorSelected = Icons.Rounded.Insights
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
