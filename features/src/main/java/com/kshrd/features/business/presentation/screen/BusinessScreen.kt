package com.kshrd.features.business.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.core.session.PortalChrome
import com.kshrd.core.session.WorkspaceState
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.coreui.components.button.PrimaryButton
import com.kshrd.coreui.components.button.SecondaryButton
import com.kshrd.coreui.components.card.AvatarView
import com.kshrd.coreui.components.card.GlassBlock
import com.kshrd.coreui.components.card.IconTile
import com.kshrd.coreui.components.card.Pill
import com.kshrd.coreui.components.card.WbCard
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.feedback.ToastData
import com.kshrd.coreui.components.feedback.WbToastHost
import com.kshrd.coreui.components.form.LabeledField
import com.kshrd.coreui.components.nav.dockContentPadding
import com.kshrd.coreui.components.text.Section
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Radius
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.paragraph
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessProfile
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.presentation.components.OpeningHoursDropdown
import com.kshrd.features.business.presentation.components.OpeningHoursEditor
import com.kshrd.features.business.presentation.components.PortalTitle
import com.kshrd.features.business.presentation.components.PortalTopBar
import com.kshrd.features.business.presentation.components.SiteHeaderCard
import com.kshrd.features.business.presentation.components.initialsOf
import com.kshrd.features.business.presentation.model.BusinessEffect
import com.kshrd.features.business.presentation.model.BusinessEvent
import com.kshrd.features.business.presentation.model.BusinessUiState
import com.kshrd.features.business.presentation.viewmodel.BusinessViewModel
import java.time.DayOfWeek

/**
 * Business information tab. Editing happens in place (as on iOS), so [onEditInfo] and [onEditHours] are
 * only kept for the existing navigation graph; the separate edit screens are no longer opened from here.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun BusinessScreen(
    onEditInfo: () -> Unit,
    onEditHours: () -> Unit,
    onOpenNotifications: () -> Unit,
    onBackToWebsites: () -> Unit,
    viewModel: BusinessViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            is BusinessEffect.OpenMaps -> runCatching { uriHandler.openUri(effect.url) }
            BusinessEffect.OpenNotifications -> onOpenNotifications()
            BusinessEffect.BackToWebsites -> onBackToWebsites()
        }
    }
    BackHandler(enabled = state.isEditing) { viewModel.onEvent(BusinessEvent.BackClicked) }
    BusinessContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun BusinessContent(
    state: BusinessUiState,
    onEvent: (BusinessEvent) -> Unit,
) {
    val website = state.chrome.workspace.current
    Box(modifier = Modifier.fillMaxSize()) {
        LiquidBackdrop(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                PortalTopBar(
                    ownerName = state.ownerName,
                    unreadCount = state.chrome.unreadCount,
                    onBack = { onEvent(BusinessEvent.BackClicked) },
                    onBell = { onEvent(BusinessEvent.BellClicked) },
                )
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Space.screen)
                            .padding(top = Space.xs, bottom = dockContentPadding()),
                    verticalArrangement = Arrangement.spacedBy(Space.m),
                ) {
                    // The large title scrolls away with the content, like the iOS navigation title.
                    PortalTitle(stringResource(R.string.bm_business_title), horizontalPadding = 0.dp)
                    Text(
                        stringResource(R.string.bm_business_subtitle, website?.domain ?: stringResource(R.string.bm_your_website)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.wb.muted,
                    )
                    SiteHeaderCard(
                        website = website,
                        websites = state.chrome.workspace.websites,
                        onSwitch = { onEvent(BusinessEvent.WebsiteSwitched(it)) },
                    )
                    when (val business = state.business) {
                        is DataState.Success ->
                            if (state.isEditing) {
                                BusinessEditor(business.data, state, onEvent)
                            } else {
                                BusinessViewer(business.data, website, onEvent)
                            }
                        is DataState.Error -> ErrorStateView(message = stringResource(R.string.bm_business_error), onRetry = {})
                        else -> LoadingView()
                    }
                }
            }
        }
        val toast = state.toast
        WbToastHost(
            toast = toast?.let { ToastData(it.kind, stringResource(it.title), stringResource(it.message), it.id) },
            onDismiss = { onEvent(BusinessEvent.ToastDismissed) },
        )
    }
}

// ---- View mode -------------------------------------------------------------------------------

@Composable
private fun BusinessViewer(
    info: BusinessInfo,
    website: WorkspaceWebsite?,
    onEvent: (BusinessEvent) -> Unit,
) {
    val profile = info.profile
    val wb = MaterialTheme.wb
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        WbCard {
            Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    AvatarView(initials = website?.let { initialsOf(it.name) } ?: initialsOf(profile.name), size = 56.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
                        Text(profile.name, style = MaterialTheme.typography.titleLarge, color = wb.text)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
                            Icon(Icons.Outlined.Language, contentDescription = null, tint = wb.muted, modifier = Modifier.size(14.dp))
                            Text(profile.url, style = MaterialTheme.typography.labelMedium, color = wb.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Pill(text = profile.type, icon = Icons.Outlined.Storefront)
                    }
                }
                PrimaryButton(text = stringResource(R.string.bm_edit_details), onClick = { onEvent(BusinessEvent.EditInfoClicked) })
            }
        }

        Section(stringResource(R.string.bm_details)) {
            GlassBlock {
                DetailHeader(Icons.Outlined.Info, stringResource(R.string.bm_about))
                Text(profile.about, style = MaterialTheme.typography.paragraph, color = wb.text.copy(alpha = 0.9f))
            }
        }
        GlassBlock {
            DetailHeader(Icons.Outlined.Phone, stringResource(R.string.bm_contact))
            Column {
                InfoLine(Icons.Outlined.Phone, stringResource(R.string.bm_phone), profile.phone) {
                    runCatching { uriHandler.openUri("tel:" + profile.phone.filter { it.isDigit() || it == '+' }) }
                }
                HorizontalDivider(color = wb.border, modifier = Modifier.padding(start = 52.dp))
                InfoLine(Icons.Outlined.Email, stringResource(R.string.bm_email), profile.email) {
                    runCatching { uriHandler.openUri("mailto:" + profile.email) }
                }
            }
        }
        GlassBlock {
            DetailHeader(Icons.Outlined.Place, stringResource(R.string.bm_location))
            Column {
                InfoLine(Icons.Outlined.Place, stringResource(R.string.bm_address), profile.address)
                if (profile.mapsLink.isNotEmpty()) {
                    HorizontalDivider(color = wb.border, modifier = Modifier.padding(start = 52.dp))
                    InfoLine(Icons.Outlined.Map, stringResource(R.string.bm_map), stringResource(R.string.bm_open_maps)) {
                        onEvent(BusinessEvent.OpenMapsClicked)
                    }
                }
            }
        }

        OpeningHoursDropdown(hours = info.hours)
    }
}

/** Card heading: a small tinted icon tile and the section title. */
@Composable
private fun DetailHeader(
    icon: ImageVector,
    title: String,
) {
    val wb = MaterialTheme.wb
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        IconTile(icon, size = 36.dp)
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = wb.text)
    }
}

/** One row: round icon tile, muted label over the value. Tappable rows turn blue and show an arrow. */
@Composable
private fun InfoLine(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val wb = MaterialTheme.wb
    val clickable = if (onClick != null) Modifier.clip(RoundedCornerShape(Radius.tile)).clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier = Modifier.fillMaxWidth().then(clickable).padding(vertical = Space.s),
        horizontalArrangement = Arrangement.spacedBy(Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(wb.brandSoft.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = wb.brand, modifier = Modifier.size(Sizes.icon))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xxxs)) {
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal), color = wb.muted)
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (onClick == null) FontWeight.Normal else FontWeight.SemiBold),
                color = if (onClick == null) wb.text else wb.brand,
            )
        }
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = wb.muted, modifier = Modifier.size(Sizes.icon))
        }
    }
}

// ---- Edit mode -------------------------------------------------------------------------------

@Composable
private fun BusinessEditor(
    info: BusinessInfo,
    state: BusinessUiState,
    onEvent: (BusinessEvent) -> Unit,
) {
    val profile = info.profile
    val draft = state.draft
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        // Name, type and web address are not editable.
        LabeledField(stringResource(R.string.bm_business_name), profile.name, {}, readOnly = true)
        LabeledField(stringResource(R.string.bm_business_type), profile.type, {}, readOnly = true)
        LabeledField(stringResource(R.string.bm_website_address), profile.url, {}, readOnly = true)
        LabeledField(
            title = stringResource(R.string.bm_description),
            value = draft.about,
            onValueChange = { onEvent(BusinessEvent.AboutChanged(it)) },
            minLines = 3,
        )
        LabeledField(
            title = stringResource(R.string.bm_phone),
            value = draft.phone,
            onValueChange = { onEvent(BusinessEvent.PhoneChanged(it)) },
            keyboardType = KeyboardType.Phone,
        )
        LabeledField(
            title = stringResource(R.string.bm_email),
            value = draft.email,
            onValueChange = { onEvent(BusinessEvent.EmailChanged(it)) },
            keyboardType = KeyboardType.Email,
            capitalization = KeyboardCapitalization.None,
        )
        LabeledField(
            title = stringResource(R.string.bm_address),
            value = draft.address,
            onValueChange = { onEvent(BusinessEvent.AddressChanged(it)) },
            minLines = 2,
        )
        LabeledField(
            title = stringResource(R.string.bm_maps_link),
            value = draft.mapsLink,
            onValueChange = { onEvent(BusinessEvent.MapsChanged(it)) },
            isOptional = true,
            keyboardType = KeyboardType.Uri,
            capitalization = KeyboardCapitalization.None,
            hint = stringResource(R.string.bm_maps_hint),
        )
        Section(stringResource(R.string.bm_opening_hours)) {
            OpeningHoursEditor(
                days = state.hoursDraft,
                onToggle = { day, open -> onEvent(BusinessEvent.DayToggled(day, open)) },
                onOpenChanged = { day, minutes -> onEvent(BusinessEvent.OpenChanged(day, minutes)) },
                onCloseChanged = { day, minutes -> onEvent(BusinessEvent.CloseChanged(day, minutes)) },
            )
        }
        // Same gap between paired buttons as the product editor and the product page.
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            SecondaryButton(
                text = stringResource(R.string.bm_cancel),
                onClick = { onEvent(BusinessEvent.CancelEditing) },
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                text = stringResource(R.string.bm_save_changes),
                onClick = { onEvent(BusinessEvent.SaveClicked) },
                loading = state.isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ---- Previews --------------------------------------------------------------------------------

private val previewInfo =
    BusinessInfo(
        BusinessProfile(
            name = "Petal & Stem",
            type = "Florist",
            url = "petalandstem.webblock.com",
            phone = "012 345 678",
            email = "hello@petalandstem.com",
            address = "18 Garden Lane, Phnom Penh",
            mapsLink = "https://maps.app.goo.gl/PetalStem18",
            about = "Hand-tied bouquets, single stems and event flowers, arranged in our studio and delivered across town.",
        ),
        DayOfWeek.entries.map { DayHours(it, it.value <= 5, 9 * 60, 18 * 60) },
    )

private val previewChrome =
    PortalChrome(
        WorkspaceState(
            current = WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com"),
            websites = listOf(WorkspaceWebsite("1", "Petal & Stem", "petalandstem.webblock.com")),
        ),
        unreadCount = 1,
    )

@ThemePreviews
@Composable
private fun BusinessContentPreview() {
    PreviewSurface(padding = 0) {
        BusinessContent(
            state = BusinessUiState(chrome = previewChrome, ownerName = "Maya Sok", business = DataState.Success(previewInfo)),
            onEvent = {},
        )
    }
}

@ThemePreviews
@Composable
private fun BusinessEditorPreview() {
    val p = previewInfo.profile
    PreviewSurface(padding = 0) {
        BusinessContent(
            state =
                BusinessUiState(
                    chrome = previewChrome,
                    ownerName = "Maya Sok",
                    business = DataState.Success(previewInfo),
                    isEditing = true,
                    draft = BusinessDraft(p.phone, p.email, p.address, p.mapsLink, p.about),
                    hoursDraft = previewInfo.hours,
                ),
            onEvent = {},
        )
    }
}
