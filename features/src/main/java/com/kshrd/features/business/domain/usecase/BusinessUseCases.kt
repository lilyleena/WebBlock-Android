package com.kshrd.features.business.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.core.session.SessionOwner
import com.kshrd.core.session.SessionStore
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessFormError
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessSaveIssue
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.DayOfWeek
import javax.inject.Inject

object BusinessValidator {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(draft: BusinessDraft): Set<BusinessFormError> =
        buildSet {
            if (draft.phone.isBlank()) add(BusinessFormError.PhoneEmpty)
            if (!emailRegex.matches(draft.email.trim())) add(BusinessFormError.EmailInvalid)
            if (draft.address.isBlank()) add(BusinessFormError.AddressEmpty)
        }

    /** The one problem to tell the owner about when saving the whole form, or null when it can be saved. */
    fun firstIssue(draft: BusinessDraft): BusinessSaveIssue? =
        when {
            draft.about.isBlank() -> BusinessSaveIssue.AboutEmpty
            draft.phone.isBlank() -> BusinessSaveIssue.PhoneEmpty
            !draft.email.contains("@") -> BusinessSaveIssue.EmailInvalid
            else -> null
        }
}

/** Pure rules for editing the weekly schedule. */
object OpeningHoursRules {
    private val weekdays = listOf(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

    /** "Copy Monday's hours to all weekdays". */
    fun copyMondayToWeekdays(hours: List<DayHours>): List<DayHours> {
        val monday = hours.firstOrNull { it.day == DayOfWeek.MONDAY } ?: return hours
        return hours.map { if (it.day in weekdays) it.copy(isOpen = monday.isOpen, openMinutes = monday.openMinutes, closeMinutes = monday.closeMinutes) else it }
    }

    /** Opening time must stay before closing time; the other end is nudged by 30 minutes if needed. */
    fun withOpen(
        hours: DayHours,
        minutes: Int,
    ): DayHours = hours.copy(openMinutes = minutes, closeMinutes = maxOf(hours.closeMinutes, minutes + STEP))

    fun withClose(
        hours: DayHours,
        minutes: Int,
    ): DayHours = hours.copy(closeMinutes = minutes, openMinutes = minOf(hours.openMinutes, minutes - STEP))

    const val STEP = 30
}

class ObserveBusinessUseCase
    @Inject
    constructor(
        private val repository: BusinessRepository,
    ) {
        operator fun invoke(websiteId: String): Flow<DataState<BusinessInfo>> = repository.observeBusiness(websiteId).asUseCaseFlow()
    }

class UpdateBusinessProfileUseCase
    @Inject
    constructor(
        private val repository: BusinessRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            draft: BusinessDraft,
        ): Flow<DataState<Unit>> {
            if (BusinessValidator.validate(draft).isNotEmpty()) return flowOf(DataState.Error(message = "Please complete the form."))
            return repository.updateProfile(websiteId, draft).asUseCaseFlow()
        }
    }

class UpdateOpeningHoursUseCase
    @Inject
    constructor(
        private val repository: BusinessRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> = repository.updateHours(websiteId, hours).asUseCaseFlow()
    }

class SaveBusinessInfoUseCase
    @Inject
    constructor(
        private val repository: BusinessRepository,
    ) {
        operator fun invoke(
            websiteId: String,
            draft: BusinessDraft,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> {
            if (BusinessValidator.firstIssue(draft) != null) return flowOf(DataState.Error(message = "Please complete the form."))
            return repository.saveAll(websiteId, draft, hours).asUseCaseFlow()
        }
    }

/** The signed-in owner, for the portal greeting and avatar. */
class ObserveOwnerUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke(): Flow<SessionOwner?> = session.owner
    }
