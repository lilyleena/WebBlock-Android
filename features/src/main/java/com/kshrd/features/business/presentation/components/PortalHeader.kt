package com.kshrd.features.business.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.card.AvatarView
import com.kshrd.coreui.components.card.LogoAvatar
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.cardShadow
import com.kshrd.coreui.components.card.glass
import com.kshrd.coreui.components.nav.UnreadBadge
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

/**
 * Portal toolbar: "‹ Websites" on the left, then the notification bell (with the same unread count badge
 * as the websites list) and the account avatar, each in a glass circle.
 */
@Composable
fun PortalTopBar(
    ownerName: String,
    unreadCount: Int,
    onBack: () -> Unit,
    onBell: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Space.screen, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clip(PillShape)
                    .clickable(onClick = onBack)
                    .padding(end = Space.xs, top = Space.xs, bottom = Space.xs),
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBackIos, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.iconSmall))
            Text(
                stringResource(R.string.bm_websites),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = wb.brand,
                modifier = Modifier.padding(start = Space.xxxs),
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(Sizes.compact)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBell),
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = stringResource(R.string.cd_notifications), tint = wb.brand, modifier = Modifier.size(Sizes.iconLarge))
            if (unreadCount > 0) UnreadBadge(count = unreadCount, ring = wb.card, modifier = Modifier.align(Alignment.TopEnd))
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(start = Space.xs).size(Sizes.compact)) {
            AvatarView(initials = initialsOf(ownerName), size = 40.dp)
        }
    }
}

/** iOS large navigation title ("Business information", "Monitor"). */
@Composable
fun PortalTitle(
    text: String,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = Space.screen,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.wb.text,
        modifier = modifier.fillMaxWidth().padding(horizontal = horizontalPadding).padding(top = Space.xxs, bottom = Space.xs).semantics { heading() },
    )
}

/** Up to two initials: "Petal & Stem" -> "PS". */
fun initialsOf(name: String): String =
    name
        .split(' ')
        .mapNotNull { part -> part.firstOrNull { it.isLetter() } }
        .take(2)
        .joinToString("") { it.uppercase() }

/** iOS `WebsiteLogo`: the bundled logo when we have one for this address, otherwise a gradient initials disc. */
@Composable
fun WebsiteLogoMark(
    website: WorkspaceWebsite,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    LogoAvatar(logoRes = logoFor(website.domain), initials = initialsOf(website.name), modifier = modifier, size = size)
}

private fun logoFor(domain: String): Int? =
    when {
        "petal" in domain -> R.drawable.petal_stem_logo
        "golden" in domain || "crumb" in domain -> R.drawable.golden_crumb_logo
        "bean" in domain || "brew" in domain -> R.drawable.bean_brew_logo
        "lotus" in domain || "yoga" in domain -> R.drawable.studio_lotus_logo
        else -> null
    }

/**
 * iOS `SiteHeaderCard`: logo, name and address with a green "Live" pill. With more than one live website
 * it drops down the list so you can switch without leaving the tab.
 */
@Composable
fun SiteHeaderCard(
    website: WorkspaceWebsite?,
    websites: List<WorkspaceWebsite>,
    onSwitch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (website == null) return
    val wb = MaterialTheme.wb
    val multiple = websites.size > 1
    var expanded by remember { mutableStateOf(false) }
    val borderAlpha by animateFloatAsState(if (expanded) 0.4f else 0.16f, label = "site-card-border")
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.78f), label = "site-card-chevron")
    // White glass panel with a faint lavender outline, so it sits on the backdrop like the search bar.
    val shape = RoundedCornerShape(Radius.panel)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .cardShadow(shape)
                .glass(shape, tint = if (expanded) wb.brandSoft.copy(alpha = 0.5f) else null)
                .border(BorderStroke(1.dp, wb.brand.copy(alpha = borderAlpha)), shape)
                .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = multiple,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expanded = !expanded }.padding(Space.s),
        ) {
            WebsiteLogoMark(website, 44.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
                Text(website.name, style = MaterialTheme.typography.titleSmall, color = wb.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(website.domain, style = MaterialTheme.typography.bodySmall, color = wb.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Pill(
                text = stringResource(R.string.bm_live),
                icon = Icons.Outlined.Language,
                tint = wb.green,
                background = wb.greenSoft,
            )
            if (multiple) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.icon).rotate(chevron))
            }
        }
        AnimatedVisibility(visible = expanded && multiple) {
            Column(
                modifier = Modifier.padding(horizontal = Space.s).padding(bottom = Space.s),
                verticalArrangement = Arrangement.spacedBy(Space.xs),
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(wb.border))
                websites.forEach { option ->
                    val current = option.id == website.id
                    SiteOptionRow(option, current) {
                        expanded = false
                        if (!current) onSwitch(option.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun SiteOptionRow(
    option: WorkspaceWebsite,
    current: Boolean,
    onClick: () -> Unit,
) {
    val wb = MaterialTheme.wb
    val shape = RoundedCornerShape(Radius.tile)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (current) wb.brand.copy(alpha = 0.10f) else wb.card.copy(alpha = 0.6f), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = Space.xs, vertical = Space.xs),
    ) {
        WebsiteLogoMark(option, 32.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                option.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (current) FontWeight.SemiBold else FontWeight.Medium),
                color = wb.text,
            )
            Text(option.domain, style = MaterialTheme.typography.labelMedium, color = wb.muted)
        }
        if (current) Icon(Icons.Filled.Check, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.icon))
    }
}

@ThemePreviews
@Composable
private fun PortalHeaderPreview() {
    PreviewSurface(padding = 0) {
        Column {
            PortalTopBar(ownerName = "Maya Sok", unreadCount = 3, onBack = {}, onBell = {})
            PortalTitle("Business information")
            val sites =
                listOf(
                    WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com"),
                    WorkspaceWebsite("2", "Golden Crumb Bakery", "goldencrumb.webblock.com"),
                )
            SiteHeaderCard(sites.first(), sites, onSwitch = {}, modifier = Modifier.padding(horizontal = Space.screen))
        }
    }
}
