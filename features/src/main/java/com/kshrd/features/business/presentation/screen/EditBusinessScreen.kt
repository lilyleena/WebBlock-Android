package com.kshrd.features.business.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kshrd.core.presentation.OnEachEvent
import com.kshrd.coreui.components.button.AppTextAction
import com.kshrd.coreui.components.card.FormCard
import com.kshrd.coreui.components.card.IconTile
import com.kshrd.coreui.components.form.LineTextField
import com.kshrd.coreui.components.nav.BackTopBar
import com.kshrd.coreui.icons.WebIcons
import com.kshrd.coreui.preview.PreviewSurface
import com.kshrd.coreui.preview.ThemePreviews
import com.kshrd.coreui.theme.Sizes
import com.kshrd.coreui.theme.Space
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessFormError
import com.kshrd.features.business.presentation.model.EditBusinessEffect
import com.kshrd.features.business.presentation.model.EditBusinessEvent
import com.kshrd.features.business.presentation.model.EditBusinessUiState
import com.kshrd.features.business.presentation.viewmodel.EditBusinessViewModel

@Composable
fun EditBusinessScreen(
    onBack: () -> Unit,
    viewModel: EditBusinessViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnEachEvent(viewModel.effects) { effect ->
        when (effect) {
            EditBusinessEffect.Back -> onBack()
        }
    }
    EditBusinessContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun EditBusinessContent(
    state: EditBusinessUiState,
    onEvent: (EditBusinessEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer).statusBarsPadding().imePadding()) {
        BackTopBar(
            backLabel = stringResource(R.string.business_title),
            onBack = { onEvent(EditBusinessEvent.BackClicked) },
            title = stringResource(R.string.edit_business_title),
            trailing = {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = Space.m).size(Sizes.icon), strokeWidth = 2.dp)
                } else {
                    AppTextAction(
                        text = stringResource(R.string.action_save),
                        onClick = { onEvent(EditBusinessEvent.SaveClicked) },
                        enabled = state.loaded,
                    )
                }
            },
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Space.screen, vertical = Space.xs),
            verticalArrangement = Arrangement.spacedBy(Space.m),
        ) {
            val draft = state.draft
            FormCard {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.s),
                ) {
                    IconTile(WebIcons.Lock, size = 44.dp)
                    Text(
                        stringResource(R.string.edit_business_locked_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            FormCard(title = stringResource(R.string.business_section_contact)) {
                LineTextField(
                    value = draft.phone,
                    onValueChange = { onEvent(EditBusinessEvent.PhoneChanged(it)) },
                    label = stringResource(R.string.edit_business_phone),
                    keyboardType = KeyboardType.Phone,
                    error = if (BusinessFormError.PhoneEmpty in state.errors) stringResource(R.string.error_field_empty) else null,
                )
                LineTextField(
                    value = draft.email,
                    onValueChange = { onEvent(EditBusinessEvent.EmailChanged(it)) },
                    label = stringResource(R.string.edit_business_email),
                    keyboardType = KeyboardType.Email,
                    error = if (BusinessFormError.EmailInvalid in state.errors) stringResource(R.string.error_email_invalid) else null,
                )
                LineTextField(
                    value = draft.address,
                    onValueChange = { onEvent(EditBusinessEvent.AddressChanged(it)) },
                    label = stringResource(R.string.edit_business_address),
                    error = if (BusinessFormError.AddressEmpty in state.errors) stringResource(R.string.error_field_empty) else null,
                )
                LineTextField(
                    value = draft.mapsLink,
                    onValueChange = { onEvent(EditBusinessEvent.MapsChanged(it)) },
                    label = stringResource(R.string.edit_business_maps),
                    keyboardType = KeyboardType.Uri,
                )
            }
            FormCard(title = stringResource(R.string.business_section_about)) {
                LineTextField(
                    value = draft.about,
                    onValueChange = { onEvent(EditBusinessEvent.AboutChanged(it)) },
                    label = stringResource(R.string.edit_business_about),
                    minLines = 4,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun EditBusinessContentPreview() {
    PreviewSurface(padding = 0) {
        EditBusinessContent(
            state =
                EditBusinessUiState(
                    loaded = true,
                    draft = BusinessDraft("012 345 678", "bad-email", "No. 24, Street 240", "https://maps.app.goo.gl/x", "About us"),
                    errors = setOf(BusinessFormError.EmailInvalid),
                ),
            onEvent = {},
        )
    }
}
