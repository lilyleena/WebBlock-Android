package com.kshrd.features.business.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.DayHours
import kotlinx.coroutines.flow.Flow

interface BusinessRepository {
    fun observeBusiness(websiteId: String): Flow<DataState<BusinessInfo>>

    fun updateProfile(
        websiteId: String,
        draft: BusinessDraft,
    ): Flow<DataState<Unit>>

    fun updateHours(
        websiteId: String,
        hours: List<DayHours>,
    ): Flow<DataState<Unit>>

    /** Saves the editable details and the opening hours in one go (the inline editor on the Business tab). */
    fun saveAll(
        websiteId: String,
        draft: BusinessDraft,
        hours: List<DayHours>,
    ): Flow<DataState<Unit>>
}
