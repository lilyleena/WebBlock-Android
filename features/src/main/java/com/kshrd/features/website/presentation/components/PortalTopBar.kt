package com.kshrd.features.website.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshrd.coreui.R as CoreR
import com.kshrd.coreui.components.brand.WebBlocksLogoInline
import com.kshrd.coreui.components.brand.WebBlocksMark
import com.kshrd.coreui.components.card.AvatarView
import com.kshrd.coreui.components.menu.WbDropdownMenu
import com.kshrd.coreui.components.menu.WbMenuItem
import com.kshrd.coreui.components.nav.UnreadBadge
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.PillShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R

// "Blocks" in the wordmark on the brand header: the logo's violet, lightened for white-on-blue.
private val WordLavender = Color(0xFFDCD6FF)
private val TileGlow = Color(0xFF6D4CFF)

/**
 * Toolbar on the websites list. On the brand header ([onBrand]) it shows the logo mark on a glowing
 * white tile beside a "WebBlocks" wordmark and a small "Business portal" tagline, with the bell (and
 * its unread count) and the account avatar grouped in one frosted-glass capsule on the right. On a
 * plain page it falls back to the inline logo and bare icons. [ownerName] labels the avatar menu.
 */
@Composable
fun PortalTopBar(
    ownerName: String,
    ownerInitials: String,
    unreadCount: Int,
    onBellClick: () -> Unit,
    onReplayIntro: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onBrand: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(horizontal = Space.screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (onBrand) BrandLockup() else WebBlocksLogoInline(height = 36.dp)
        }
        ActionCapsule(
            ownerName = ownerName,
            ownerInitials = ownerInitials,
            unreadCount = unreadCount,
            onBellClick = onBellClick,
            onReplayIntro = onReplayIntro,
            onLogout = onLogout,
            onBrand = onBrand,
        )
    }
}

/** Logo mark on a white tile with a violet glow, then the wordmark over a small tagline. */
@Composable
private fun BrandLockup() {
    val description = stringResource(CoreR.string.wb_logo_description)
    val tile = RoundedCornerShape(13.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(42.dp)
                    .shadow(10.dp, tile, ambientColor = TileGlow, spotColor = TileGlow)
                    .background(Color.White, tile)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), tile),
        ) {
            WebBlocksMark(width = 28.dp, trimmed = true)
        }
        Column {
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) { append(stringResource(CoreR.string.wb_logo_word_web)) }
                        withStyle(SpanStyle(color = WordLavender)) { append(stringResource(CoreR.string.wb_logo_word_blocks)) }
                    },
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.home_header_tagline).uppercase(),
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
            )
        }
    }
}

/** Bell (with unread count) and account avatar side by side; on the brand header, inside a glass capsule. */
@Composable
private fun ActionCapsule(
    ownerName: String,
    ownerInitials: String,
    unreadCount: Int,
    onBellClick: () -> Unit,
    onReplayIntro: () -> Unit,
    onLogout: () -> Unit,
    onBrand: Boolean,
) {
    val wb = MaterialTheme.wb
    var menuOpen by remember { mutableStateOf(false) }
    val account = stringResource(R.string.home_account_menu, ownerName)
    val bellCd = stringResource(R.string.home_notifications_cd, unreadCount)
    val capsule = PillShape
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxxs),
        modifier =
            if (onBrand) {
                Modifier
                    .background(Color.White.copy(alpha = 0.16f), capsule)
                    .border(1.dp, Color.White.copy(alpha = 0.32f), capsule)
                    .padding(3.dp)
            } else {
                Modifier
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(Sizes.compact)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBellClick)
                    .semantics { contentDescription = bellCd },
        ) {
            Icon(
                WebIcons.Bell,
                contentDescription = null,
                tint = if (onBrand) Color.White else wb.text,
                modifier = Modifier.size(23.dp),
            )
            if (unreadCount > 0) {
                UnreadBadge(count = unreadCount, ring = if (onBrand) HeaderRing else wb.card, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(Sizes.compact)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { menuOpen = true }
                    .semantics { contentDescription = account },
        ) {
            // The avatar's glass tint disappears on blue, so it sits on a white disc there.
            Box(Modifier.size(40.dp).background(if (onBrand) Color.White else Color.Transparent, CircleShape)) {
                AvatarView(ownerInitials, size = 40.dp)
            }
            WbDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                WbMenuItem(
                    icon = Icons.Outlined.PlayCircle,
                    label = stringResource(R.string.home_replay_intro),
                    onClick = {
                        menuOpen = false
                        onReplayIntro()
                    },
                )
                WbMenuItem(
                    icon = WebIcons.LogOut,
                    label = stringResource(R.string.home_log_out),
                    destructive = true,
                    onClick = {
                        menuOpen = false
                        onLogout()
                    },
                )
            }
        }
    }
}

// Ring colour around the badge on the brand header: the header blue seen through the glass capsule,
// so the badge looks cut out of it.
private val HeaderRing = Color(0xFF4865E2)

@ThemePreviews
@Composable
private fun PortalTopBarPreview() {
    PreviewSurface(padding = 0) {
        Box(Modifier.portalHeaderBackdrop(MaterialTheme.wb.background, 380.dp)) {
            PortalTopBar(
                ownerName = "Sovannarith Soeun",
                ownerInitials = "SS",
                unreadCount = 4,
                onBellClick = {},
                onReplayIntro = {},
                onLogout = {},
                onBrand = true,
            )
        }
    }
}
