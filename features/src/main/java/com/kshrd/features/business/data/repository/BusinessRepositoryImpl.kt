package com.kshrd.features.business.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.business.data.source.InMemoryBusinessDataSource
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.repository.BusinessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BusinessRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryBusinessDataSource,
    ) : BusinessRepository {
        override fun observeBusiness(websiteId: String): Flow<DataState<BusinessInfo>> =
            flow {
                delay(LOAD_LATENCY_MS)
                emitAll(source.info(websiteId).map { DataState.Success(it) })
            }

        override fun updateProfile(
            websiteId: String,
            draft: BusinessDraft,
        ): Flow<DataState<Unit>> =
            flow {
                source.updateProfile(websiteId, draft)
                emit(DataState.Success(Unit))
            }

        override fun updateHours(
            websiteId: String,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> =
            flow {
                source.updateHours(websiteId, hours)
                emit(DataState.Success(Unit))
            }

        override fun saveAll(
            websiteId: String,
            draft: BusinessDraft,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> =
            flow {
                source.updateProfile(websiteId, draft)
                source.updateHours(websiteId, hours)
                emit(DataState.Success(Unit))
            }

        private companion object {
            const val LOAD_LATENCY_MS = 500L
        }
    }
