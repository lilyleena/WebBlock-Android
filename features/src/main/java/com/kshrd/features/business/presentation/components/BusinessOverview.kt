package com.kshrd.features.business.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.card.BadgeTone
import com.kshrd.coreui.components.card.StatusBadge
import com.kshrd.coreui.components.card.WbCard
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.BrandGradientColors
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.paragraph
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.BusinessProfile
import com.kshrd.features.business.domain.model.DayHours
import java.time.DayOfWeek
import java.time.LocalDateTime

// ---- Open / closed status ----------------------------------------------------------------------

/** Whether the business is open at a moment, and the next change ("closes 6:00 PM" / "opens 9:00 AM"). */
@Immutable
data class OpenStatus(
    val isOpen: Boolean,
    /** Minutes after midnight of the next open/close, or null when there is none in the coming week. */
    val nextChangeMinutes: Int?,
)

/** Works out [OpenStatus] from the weekly [hours] at [now]. Pure, so it is unit-testable. */
fun openStatus(
    hours: List<DayHours>,
    now: LocalDateTime,
): OpenStatus {
    val minute = now.hour * 60 + now.minute
    val today = hours.firstOrNull { it.day == now.dayOfWeek }
    if (today != null && today.isOpen && minute in today.openMinutes until today.closeMinutes) {
        return OpenStatus(isOpen = true, nextChangeMinutes = today.closeMinutes)
    }
    // Closed: later today if it has not opened yet, otherwise the next open day.
    if (today != null && today.isOpen && minute < today.openMinutes) return OpenStatus(false, today.openMinutes)
    val next =
        (1..7)
            .map { now.dayOfWeek.plus(it.toLong()) }
            .firstNotNullOfOrNull { d -> hours.firstOrNull { it.day == d && it.isOpen } }
    return OpenStatus(isOpen = false, nextChangeMinutes = next?.openMinutes)
}

/** "Open now · Closes 6:00 PM" as a green pill, or "Closed now · Opens 9:00 AM" in neutral. */
@Composable
fun OpenStatusPill(
    status: OpenStatus,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(if (status.isOpen) R.string.bm_open_now else R.string.bm_closed_now)
    val next =
        status.nextChangeMinutes?.let {
            stringResource(if (status.isOpen) R.string.bm_closes_at else R.string.bm_opens_at, formatMinutes(it))
        }
    StatusBadge(
        text = if (next != null) "$label · $next" else label,
        tone = if (status.isOpen) BadgeTone.Success else BadgeTone.Neutral,
        icon = WebIcons.Clock,
        modifier = modifier,
    )
}

// ---- Storefront card ---------------------------------------------------------------------------

private val BannerHeight = 92.dp
private val AvatarSize = 68.dp

/**
 * The business as customers meet it: a brand banner with the blueprint grid (as on the web sign-in
 * panel), the initials avatar overlapping its lower edge, then name, web address, type, today's
 * open/closed status and the Edit details button.
 */
@Composable
fun StorefrontCard(
    profile: BusinessProfile,
    initials: String,
    status: OpenStatus,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    WbCard(modifier = modifier, padding = 0.dp) {
        Box {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(BannerHeight)
                    .clip(RoundedCornerShape(topStart = Radius.card, topEnd = Radius.card))
                    .background(Brush.linearGradient(BrandGradientColors))
                    .drawBehind { drawBannerGrid() },
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .padding(start = Space.m)
                        .offset(y = BannerHeight - AvatarSize / 2)
                        .size(AvatarSize)
                        .border(4.dp, wb.card, CircleShape)
                        .padding(4.dp)
                        .background(wb.brandSoft, CircleShape),
            ) {
                Text(initials, style = MaterialTheme.typography.headlineSmall, color = wb.brand)
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.xs),
            modifier = Modifier.padding(start = Space.m, end = Space.m, bottom = Space.m, top = AvatarSize / 2 + Space.xs),
        ) {
            Text(
                profile.name,
                style = MaterialTheme.typography.headlineSmall,
                color = wb.text,
                modifier = Modifier.semantics { heading() },
            )
            MetaLine(WebIcons.Globe, profile.url)
            MetaLine(WebIcons.Store, profile.type)
            OpenStatusPill(status, modifier = Modifier.padding(top = Space.xxs))
            PrimaryButton(
                text = stringResource(R.string.bm_edit_details),
                onClick = onEdit,
                icon = WebIcons.Pencil,
                modifier = Modifier.padding(top = Space.xs),
            )
        }
    }
}

/** Faint white grid with a couple of brighter cells, like the web panel, on the banner. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBannerGrid() {
    val step = 23.dp.toPx()
    val line = Color.White.copy(alpha = 0.12f)
    var x = size.width
    while (x > 0f) {
        drawLine(line, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        x -= step
    }
    var y = step
    while (y < size.height) {
        drawLine(line, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        y += step
    }
    val right = size.width - (size.width % step)
    listOf(1 to 0, 2 to 1, 0 to 2, 4 to 1, 3 to 3).forEach { (col, row) ->
        drawRect(
            Color.White.copy(alpha = 0.10f),
            topLeft = Offset(right - (col + 1) * step, row * step),
            size = androidx.compose.ui.geometry.Size(step, step),
        )
    }
}

@Composable
private fun MetaLine(
    icon: ImageVector,
    text: String,
) {
    val wb = MaterialTheme.wb
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        Icon(icon, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.iconSmall))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = wb.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---- Quick actions -----------------------------------------------------------------------------

/** One round action under the storefront card. [onClick] null shows it disabled (e.g. no maps link). */
@Immutable
data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val onClick: (() -> Unit)?,
)

/** Call / Email / Directions / Website as evenly spaced round buttons in one card. */
@Composable
fun QuickActionsRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
) {
    WbCard(modifier = modifier, padding = Space.s) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            actions.forEach { action -> QuickActionButton(action) }
        }
    }
}

@Composable
private fun QuickActionButton(action: QuickAction) {
    val wb = MaterialTheme.wb
    val enabled = action.onClick != null
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.xs),
        modifier =
            Modifier
                .widthIn(min = 64.dp)
                .clip(RoundedCornerShape(Radius.control))
                .clickable(enabled = enabled, role = Role.Button) { action.onClick?.invoke() }
                .padding(vertical = Space.xs, horizontal = Space.xxs)
                .alpha(if (enabled) 1f else 0.4f),
    ) {
        Box(Modifier.size(48.dp).background(wb.brandSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(action.icon, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.icon))
        }
        Text(action.label, style = MaterialTheme.typography.labelMedium, color = wb.text, textAlign = TextAlign.Center, maxLines = 1)
    }
}

// ---- Business details --------------------------------------------------------------------------

/**
 * The web dashboard's "Business details" panel: About, Contact and Location grouped in one card, each
 * a small section label over label/value rows. The Maps link reads as a link.
 */
@Composable
fun BusinessDetailsCard(
    profile: BusinessProfile,
    onOpenMaps: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    WbCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            Text(stringResource(R.string.bm_business_details), style = MaterialTheme.typography.titleLarge, color = wb.text)
            DetailGroup(stringResource(R.string.bm_about)) {
                Text(profile.about, style = MaterialTheme.typography.paragraph, color = wb.text)
            }
            DetailGroup(stringResource(R.string.bm_contact)) {
                ValueRow(WebIcons.Phone, stringResource(R.string.bm_phone), profile.phone)
                HorizontalDivider(color = wb.border)
                ValueRow(WebIcons.Mail, stringResource(R.string.bm_email), profile.email)
            }
            DetailGroup(stringResource(R.string.bm_location)) {
                ValueRow(WebIcons.Pin, stringResource(R.string.bm_address), profile.address)
                if (onOpenMaps != null) {
                    HorizontalDivider(color = wb.border)
                    ValueRow(WebIcons.ExternalLink, stringResource(R.string.bm_map), stringResource(R.string.bm_open_maps), onClick = onOpenMaps)
                }
            }
        }
    }
}

@Composable
private fun DetailGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.wb.brand,
            modifier = Modifier.semantics { heading() },
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.control))
                    .background(MaterialTheme.wb.canvas)
                    .padding(horizontal = Space.s, vertical = Space.xxs),
        ) { content() }
    }
}

@Composable
private fun ValueRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val wb = MaterialTheme.wb
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(vertical = Space.s),
    ) {
        Icon(icon, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.icon))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = wb.muted)
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (onClick != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (onClick != null) wb.brand else wb.text,
            )
        }
        if (onClick != null) Icon(WebIcons.ChevronRight, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.iconSmall))
    }
}

// ---- Opening hours -----------------------------------------------------------------------------

/** The whole week at a glance, as on the web; today's row is tinted and tagged, closed days are muted. */
@Composable
fun WeeklyHoursCard(
    hours: List<DayHours>,
    today: DayOfWeek,
    status: OpenStatus,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    WbCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.bm_opening_hours),
                    style = MaterialTheme.typography.titleLarge,
                    color = wb.text,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(
                    text = stringResource(if (status.isOpen) R.string.bm_open_now else R.string.bm_closed_now),
                    tone = if (status.isOpen) BadgeTone.Success else BadgeTone.Neutral,
                )
            }
            Column {
                DayOfWeek.entries.forEach { day ->
                    val h = hours.firstOrNull { it.day == day }
                    DayRow(day = day, hours = h, isToday = day == today)
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    day: DayOfWeek,
    hours: DayHours?,
    isToday: Boolean,
) {
    val wb = MaterialTheme.wb
    val open = hours?.isOpen == true
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.tile))
                .background(if (isToday) wb.brandSoft else Color.Transparent)
                .padding(horizontal = Space.s, vertical = Space.s),
    ) {
        Text(
            dayName(day),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isToday) wb.brand else wb.text,
        )
        if (isToday) {
            Text(
                stringResource(R.string.bm_today),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.background(wb.brand, CircleShape).padding(horizontal = Space.xs, vertical = Space.xxxs),
            )
        }
        Text(
            if (open && hours != null) {
                stringResource(R.string.bm_hours_range, formatMinutes(hours.openMinutes), formatMinutes(hours.closeMinutes))
            } else {
                stringResource(R.string.bm_closed)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (!open) wb.muted else if (isToday) wb.brand else wb.text,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

// ---- Previews ----------------------------------------------------------------------------------

private val previewProfile =
    BusinessProfile(
        name = "Petal & Stem",
        type = "Florist",
        url = "petalandstem.webblock.com",
        phone = "012 345 678",
        email = "hello@petalandstem.com",
        address = "18 Garden Lane, Phnom Penh",
        mapsLink = "https://maps.app.goo.gl/PetalStem18",
        about = "Hand-tied bouquets, single stems and event flowers, arranged in our studio and delivered across town.",
    )

private val previewHours = DayOfWeek.entries.map { DayHours(it, it.value <= 5, 9 * 60, 18 * 60) }

@ThemePreviews
@Composable
private fun BusinessOverviewPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
            val status = OpenStatus(isOpen = true, nextChangeMinutes = 18 * 60)
            StorefrontCard(previewProfile, initials = "PS", status = status, onEdit = {})
            QuickActionsRow(
                listOf(
                    QuickAction(WebIcons.Phone, "Call") {},
                    QuickAction(WebIcons.Mail, "Email") {},
                    QuickAction(WebIcons.Pin, "Directions", null),
                    QuickAction(WebIcons.Globe, "Website") {},
                ),
            )
            BusinessDetailsCard(previewProfile, onOpenMaps = {})
            WeeklyHoursCard(previewHours, today = DayOfWeek.WEDNESDAY, status = status)
        }
    }
}
