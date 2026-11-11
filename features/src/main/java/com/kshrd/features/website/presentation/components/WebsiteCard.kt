package com.kshrd.features.website.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kshrd.coreui.components.card.LogoAvatar
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.WbCard
import com.kshrd.coreui.components.nav.PortalIcons
import com.kshrd.coreui.motion.cardPress
import com.kshrd.coreui.motion.rememberPressSource
import com.kshrd.coreui.preview.FontScalePreviews
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.CardShape
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.model.WebsiteStatus

/** iOS `WebsiteLogo`: round logo (or a gradient initials tile) with a hairline border. */
@Composable
fun WebsiteLogo(
    website: Website,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    LogoAvatar(logoRes = website.logoRes, initials = website.initials, modifier = modifier, size = size)
}

/**
 * iOS `WebsiteCard` with press feedback: the card springs down slightly and takes a light brand wash
 * while pressed.
 */
@Composable
fun WebsiteCard(
    website: Website,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val wb = MaterialTheme.wb
    val hint = stringResource(R.string.home_open_portal_hint, website.name)
    val (source, pressed) = rememberPressSource()
    val active by animateFloatAsState(if (pressed) 1f else 0f, spring(dampingRatio = 0.68f, stiffness = 300f), label = "card-active")
    Box(modifier = modifier.fillMaxWidth().cardPress(source, pressed, scale = 0.98f, onClick = onClick).semantics { contentDescription = hint }) {
        WbCard(padding = Space.m) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    WebsiteLogo(website, size = 56.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
                        Text(
                            text = website.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = wb.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                            Icon(Icons.Outlined.Link, contentDescription = null, tint = wb.muted, modifier = Modifier.size(14.dp))
                            Text(
                                text = website.url ?: website.businessType,
                                style = MaterialTheme.typography.bodySmall,
                                color = wb.muted,
                                maxLines = 1,
                                overflow = TextOverflow.MiddleEllipsis,
                            )
                        }
                    }
                    Spacer(Modifier.width(Space.xs))
                    Pill(
                        text = stringResource(R.string.home_pill_live),
                        icon = Icons.Outlined.Public,
                        tint = wb.green,
                        background = wb.greenSoft,
                    )
                }
                HorizontalDivider(color = wb.border)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Pill(stringResource(R.string.home_chip_products), icon = PortalIcons.Products)
                    Pill(stringResource(R.string.home_chip_business), icon = PortalIcons.Business)
                    Pill(stringResource(R.string.home_chip_monitor), icon = PortalIcons.Monitor)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.icon))
                }
            }
        }
        // Press feedback: a light brand wash over the card (plus the spring-down from cardPress).
        Box(Modifier.matchParentSize().clip(CardShape).background(wb.brand.copy(alpha = 0.07f * active)))
    }
}

internal val previewWebsite =
    Website(
        id = "petal-stem",
        name = "Petal & Stem",
        businessType = "Florist",
        status = WebsiteStatus.Live,
        url = "petalandstem.webblock.com",
        logoRes = R.drawable.petal_stem_logo,
    )

@ThemePreviews
@FontScalePreviews
@Composable
private fun WebsiteCardPreview() {
    PreviewSurface { WebsiteCard(website = previewWebsite, onClick = {}) }
}

@ThemePreviews
@Composable
private fun WebsiteLogoInitialsPreview() {
    PreviewSurface { WebsiteLogo(previewWebsite.copy(logoRes = null)) }
}
