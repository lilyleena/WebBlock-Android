package com.kshrd.features.business.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.kshrd.coreui.components.decor.LiquidBackdrop
import com.kshrd.coreui.components.feedback.ErrorStateView
import com.kshrd.coreui.components.feedback.LoadingView
import com.kshrd.coreui.components.feedback.ToastData
import com.kshrd.coreui.components.feedback.WbToastHost
import com.kshrd.coreui.components.form.LabeledField
import com.kshrd.coreui.components.nav.dockContentPadding
import com.kshrd.coreui.components.text.Section
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Space
import com.kshrd.coreui.theme.wb
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessProfile
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.presentation.components.BusinessDetailsCard
import com.kshrd.features.business.presentation.components.OpeningHoursEditor
import com.kshrd.features.business.presentation.components.PortalTitle
import com.kshrd.features.business.presentation.components.PortalTopBar
import com.kshrd.features.business.presentation.components.QuickAction
import com.kshrd.features.business.presentation.components.QuickActionsRow
import com.kshrd.features.business.presentation.components.SiteHeaderCard
import com.kshrd.features.business.presentation.components.StorefrontCard
import com.kshrd.features.business.presentation.components.WeeklyHoursCard
import com.kshrd.features.business.presentation.components.initialsOf
import com.kshrd.features.business.presentation.components.openStatus
import com.kshrd.features.business.presentation.model.BusinessEffect
import com.kshrd.features.business.presentation.model.BusinessEvent
import com.kshrd.features.business.presentation.model.BusinessUiState
import com.kshrd.features.business.presentation.viewmodel.BusinessViewModel
import java.time.DayOfWeek
import java.time.LocalDateTime

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
    val uriHandler = LocalUriHandler.current
    // Re-read when the hours change; "now" is refreshed each time the tab is composed.
    val now = remember(info.hours) { LocalDateTime.now() }
    val status = remember(info.hours, now) { openStatus(info.hours, now) }
    val onOpenMaps = if (profile.mapsLink.isNotEmpty()) ({ onEvent(BusinessEvent.OpenMapsClicked) }) else null
    Column(verticalArrangement = Arrangement.spacedBy(Space.m)) {
        StorefrontCard(
            profile = profile,
            initials = website?.let { initialsOf(it.name) } ?: initialsOf(profile.name),
            status = status,
            onEdit = { onEvent(BusinessEvent.EditInfoClicked) },
        )
        QuickActionsRow(
            listOf(
                QuickAction(WebIcons.Phone, stringResource(R.string.bm_action_call)) {
                    runCatching { uriHandler.openUri("tel:" + profile.phone.filter { it.isDigit() || it == '+' }) }
                },
                QuickAction(WebIcons.Mail, stringResource(R.string.bm_action_email)) {
                    runCatching { uriHandler.openUri("mailto:" + profile.email) }
                },
                QuickAction(WebIcons.Pin, stringResource(R.string.bm_action_directions), onOpenMaps),
                QuickAction(WebIcons.Globe, stringResource(R.string.bm_action_website)) {
                    runCatching { uriHandler.openUri("https://" + profile.url) }
                },
            ),
        )
        BusinessDetailsCard(profile, onOpenMaps = onOpenMaps)
        WeeklyHoursCard(hours = info.hours, today = now.dayOfWeek, status = status)
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
