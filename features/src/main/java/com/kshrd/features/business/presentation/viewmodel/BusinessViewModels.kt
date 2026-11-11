package com.kshrd.features.business.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.OneShotEvents
import com.kshrd.core.session.ObservePortalChromeUseCase
import com.kshrd.core.session.ObserveWorkspaceUseCase
import com.kshrd.core.session.SwitchWebsiteUseCase
import com.kshrd.coreui.components.feedback.ToastKind
import com.kshrd.features.R
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessSaveIssue
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.usecase.BusinessValidator
import com.kshrd.features.business.domain.usecase.ObserveBusinessUseCase
import com.kshrd.features.business.domain.usecase.ObserveOwnerUseCase
import com.kshrd.features.business.domain.usecase.OpeningHoursRules
import com.kshrd.features.business.domain.usecase.SaveBusinessInfoUseCase
import com.kshrd.features.business.domain.usecase.UpdateBusinessProfileUseCase
import com.kshrd.features.business.presentation.model.BusinessEffect
import com.kshrd.features.business.presentation.model.BusinessEvent
import com.kshrd.features.business.presentation.model.BusinessToast
import com.kshrd.features.business.presentation.model.BusinessUiState
import com.kshrd.features.business.presentation.model.EditBusinessEffect
import com.kshrd.features.business.presentation.model.EditBusinessEvent
import com.kshrd.features.business.presentation.model.EditBusinessUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class BusinessViewModel
    @Inject
    constructor(
        private val observeChrome: ObservePortalChromeUseCase,
        private val observeOwner: ObserveOwnerUseCase,
        private val observeBusiness: ObserveBusinessUseCase,
        private val saveBusiness: SaveBusinessInfoUseCase,
        private val switchWebsite: SwitchWebsiteUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(BusinessUiState())
        val state: StateFlow<BusinessUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<BusinessEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch { observeChrome().collect { chrome -> _state.update { it.copy(chrome = chrome) } } }
            viewModelScope.launch { observeOwner().collect { owner -> _state.update { it.copy(ownerName = owner?.name.orEmpty()) } } }
            loadBusiness()
        }

        fun onEvent(event: BusinessEvent) {
            when (event) {
                BusinessEvent.EditInfoClicked -> beginEditing()
                BusinessEvent.CancelEditing -> _state.update { it.copy(isEditing = false) }
                is BusinessEvent.PhoneChanged -> edit { copy(phone = event.value) }
                is BusinessEvent.EmailChanged -> edit { copy(email = event.value) }
                is BusinessEvent.AddressChanged -> edit { copy(address = event.value) }
                is BusinessEvent.MapsChanged -> edit { copy(mapsLink = event.value) }
                is BusinessEvent.AboutChanged -> edit { copy(about = event.value) }
                is BusinessEvent.DayToggled -> editDay(event.day) { it.copy(isOpen = event.open) }
                is BusinessEvent.OpenChanged -> editDay(event.day) { OpeningHoursRules.withOpen(it, event.minutes) }
                is BusinessEvent.CloseChanged -> editDay(event.day) { OpeningHoursRules.withClose(it, event.minutes) }
                BusinessEvent.SaveClicked -> save()
                BusinessEvent.ToastDismissed -> _state.update { it.copy(toast = null) }
                BusinessEvent.OpenMapsClicked -> {
                    val link = currentInfo()?.profile?.mapsLink
                    if (!link.isNullOrBlank()) _effects.tryEmit(BusinessEffect.OpenMaps(link))
                }
                BusinessEvent.BellClicked -> _effects.tryEmit(BusinessEffect.OpenNotifications)
                is BusinessEvent.WebsiteSwitched -> {
                    _state.update { it.copy(isEditing = false) }
                    switchWebsite(event.websiteId)
                }
                BusinessEvent.BackClicked ->
                    if (_state.value.isEditing) {
                        _state.update { it.copy(isEditing = false) }
                    } else {
                        _effects.tryEmit(BusinessEffect.BackToWebsites)
                    }
            }
        }

        private fun currentInfo(): BusinessInfo? = (_state.value.business as? DataState.Success)?.data

        private fun beginEditing() {
            val info = currentInfo() ?: return
            val p = info.profile
            _state.update {
                it.copy(
                    isEditing = true,
                    draft = BusinessDraft(p.phone, p.email, p.address, p.mapsLink, p.about),
                    hoursDraft = info.hours,
                )
            }
        }

        private fun edit(transform: BusinessDraft.() -> BusinessDraft) {
            _state.update { it.copy(draft = it.draft.transform()) }
        }

        private fun editDay(
            day: DayOfWeek,
            transform: (DayHours) -> DayHours,
        ) {
            _state.update { s -> s.copy(hoursDraft = s.hoursDraft.map { if (it.day == day) transform(it) else it }) }
        }

        private fun save() {
            val current = _state.value
            if (current.isSaving) return
            val issue = BusinessValidator.firstIssue(current.draft)
            if (issue != null) {
                _state.update { it.copy(toast = issueToast(issue)) }
                return
            }
            viewModelScope.launch {
                val websiteId = current.chrome.workspace.current?.id ?: return@launch
                saveBusiness(websiteId, current.draft, current.hoursDraft).collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isSaving = true) }
                        is DataState.Success ->
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    isEditing = false,
                                    toast = BusinessToast(ToastKind.Success, R.string.bm_toast_saved_title, R.string.bm_toast_saved_message),
                                )
                            }
                        else ->
                            _state.update {
                                it.copy(
                                    isSaving = false,
                                    toast = BusinessToast(ToastKind.Error, R.string.bm_toast_save_failed_title, R.string.bm_toast_save_failed_message),
                                )
                            }
                    }
                }
            }
        }

        private fun issueToast(issue: BusinessSaveIssue): BusinessToast =
            when (issue) {
                BusinessSaveIssue.AboutEmpty -> BusinessToast(ToastKind.Error, R.string.bm_issue_about_title, R.string.bm_issue_about_message)
                BusinessSaveIssue.PhoneEmpty -> BusinessToast(ToastKind.Error, R.string.bm_issue_phone_title, R.string.bm_issue_phone_message)
                BusinessSaveIssue.EmailInvalid -> BusinessToast(ToastKind.Error, R.string.bm_issue_email_title, R.string.bm_issue_email_message)
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadBusiness() {
            viewModelScope.launch {
                observeChrome()
                    .map { it.workspace.current?.id }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .flatMapLatest { observeBusiness(it) }
                    .collect { result -> _state.update { it.copy(business = result) } }
            }
        }
    }

@HiltViewModel
class EditBusinessViewModel
    @Inject
    constructor(
        private val observeWorkspace: ObserveWorkspaceUseCase,
        private val observeBusiness: ObserveBusinessUseCase,
        private val updateProfile: UpdateBusinessProfileUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(EditBusinessUiState())
        val state: StateFlow<EditBusinessUiState> = _state.asStateFlow()

        private val _effects = OneShotEvents<EditBusinessEffect>()
        val effects = _effects.events

        init {
            viewModelScope.launch {
                val websiteId = observeWorkspace().first { it.current != null }.current?.id ?: return@launch
                val info = observeBusiness(websiteId).first { it is DataState.Success } as DataState.Success
                val p = info.data.profile
                _state.update {
                    it.copy(loaded = true, businessName = p.name, draft = BusinessDraft(p.phone, p.email, p.address, p.mapsLink, p.about))
                }
            }
        }

        fun onEvent(event: EditBusinessEvent) {
            when (event) {
                is EditBusinessEvent.PhoneChanged -> edit { copy(phone = event.value) }
                is EditBusinessEvent.EmailChanged -> edit { copy(email = event.value) }
                is EditBusinessEvent.AddressChanged -> edit { copy(address = event.value) }
                is EditBusinessEvent.MapsChanged -> edit { copy(mapsLink = event.value) }
                is EditBusinessEvent.AboutChanged -> edit { copy(about = event.value) }
                EditBusinessEvent.SaveClicked -> save()
                EditBusinessEvent.BackClicked -> _effects.tryEmit(EditBusinessEffect.Back)
            }
        }

        private fun edit(transform: BusinessDraft.() -> BusinessDraft) {
            _state.update { it.copy(draft = it.draft.transform(), errors = emptySet()) }
        }

        private fun save() {
            val current = _state.value
            if (current.isSaving || !current.loaded) return
            val errors = BusinessValidator.validate(current.draft)
            if (errors.isNotEmpty()) {
                _state.update { it.copy(errors = errors) }
                return
            }
            viewModelScope.launch {
                val websiteId = observeWorkspace().first().current?.id ?: return@launch
                updateProfile(websiteId, current.draft).collect { result ->
                    when (result) {
                        is DataState.Loading -> _state.update { it.copy(isSaving = true) }
                        is DataState.Success -> _effects.emit(EditBusinessEffect.Back)
                        else -> _state.update { it.copy(isSaving = false) }
                    }
                }
            }
        }
    }
