package com.kshrd.features.business.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kshrd.core.data.entity.DataState
import com.kshrd.core.presentation.UiState
import com.kshrd.core.session.PortalChrome
import com.kshrd.coreui.components.feedback.ToastKind
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessFormError
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.DayHours
import java.time.DayOfWeek

// ---- Business tab -------------------------------------------------------------------------

@Immutable
data class BusinessUiState(
    val chrome: PortalChrome = PortalChrome(),
    val ownerName: String = "",
    val business: UiState<BusinessInfo> = DataState.Loading,
    val isEditing: Boolean = false,
    val draft: BusinessDraft = BusinessDraft("", "", "", "", ""),
    val hoursDraft: List<DayHours> = emptyList(),
    val isSaving: Boolean = false,
    val toast: BusinessToast? = null,
)

sealed interface BusinessEvent {
    /** "Edit details": switches the page to the inline editor. */
    data object EditInfoClicked : BusinessEvent

    data object CancelEditing : BusinessEvent

    data class PhoneChanged(val value: String) : BusinessEvent

    data class EmailChanged(val value: String) : BusinessEvent

    data class AddressChanged(val value: String) : BusinessEvent

    data class MapsChanged(val value: String) : BusinessEvent

    data class AboutChanged(val value: String) : BusinessEvent

    data class DayToggled(val day: DayOfWeek, val open: Boolean) : BusinessEvent

    data class OpenChanged(val day: DayOfWeek, val minutes: Int) : BusinessEvent

    data class CloseChanged(val day: DayOfWeek, val minutes: Int) : BusinessEvent

    data object SaveClicked : BusinessEvent

    data object ToastDismissed : BusinessEvent

    data object OpenMapsClicked : BusinessEvent

    data object BellClicked : BusinessEvent

    data class WebsiteSwitched(val websiteId: String) : BusinessEvent

    /** The "‹ Websites" back button: leaves the editor first when it is open, otherwise the portal. */
    data object BackClicked : BusinessEvent
}


sealed interface BusinessEffect {
    data class OpenMaps(val url: String) : BusinessEffect

    data object OpenNotifications : BusinessEffect

    data object BackToWebsites : BusinessEffect
}

// ---- Edit business info -------------------------------------------------------------------

@Immutable
data class EditBusinessUiState(
    val loaded: Boolean = false,
    val businessName: String = "",
    val draft: BusinessDraft = BusinessDraft("", "", "", "", ""),
    val errors: Set<BusinessFormError> = emptySet(),
    val isSaving: Boolean = false,
)

sealed interface EditBusinessEvent {
    data class PhoneChanged(val value: String) : EditBusinessEvent

    data class EmailChanged(val value: String) : EditBusinessEvent

    data class AddressChanged(val value: String) : EditBusinessEvent

    data class MapsChanged(val value: String) : EditBusinessEvent

    data class AboutChanged(val value: String) : EditBusinessEvent

    data object SaveClicked : EditBusinessEvent

    data object BackClicked : EditBusinessEvent
}

sealed interface EditBusinessEffect {
    data object Back : EditBusinessEffect
}

// ---- Opening hours ------------------------------------------------------------------------

@Immutable
data class OpeningHoursUiState(
    val loaded: Boolean = false,
    val hours: List<DayHours> = emptyList(),
    val isSaving: Boolean = false,
)

sealed interface OpeningHoursEvent {
    data class DayToggled(val day: DayOfWeek, val open: Boolean) : OpeningHoursEvent

    data class OpenChanged(val day: DayOfWeek, val minutes: Int) : OpeningHoursEvent

    data class CloseChanged(val day: DayOfWeek, val minutes: Int) : OpeningHoursEvent

    data object CopyMondayClicked : OpeningHoursEvent

    data object SaveClicked : OpeningHoursEvent

    data object BackClicked : OpeningHoursEvent
}

sealed interface OpeningHoursEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : OpeningHoursEffect

    data object Back : OpeningHoursEffect
}

/** A toast described by string resources; the screen turns it into a `ToastData`. */
@Immutable
data class BusinessToast(
    val kind: ToastKind,
    @StringRes val title: Int,
    @StringRes val message: Int,
    val id: Long = System.nanoTime(),
)
