package com.kshrd.features.remote.repository

import com.kshrd.core.api.apiFlow
import com.kshrd.core.data.entity.DataState
import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.repository.BusinessRepository
import com.kshrd.features.remote.api.ProjectApi
import com.kshrd.features.remote.mapper.businessInfoBody
import com.kshrd.features.remote.mapper.toBusinessInfo
import com.kshrd.features.remote.mapper.toDraft
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Project v2 business information (docs/API-MAPPING.md 2.5). The API has one PUT for the details and the hours, so
 * the two partial saves send the whole body built from the last info read.
 */
@Singleton
class ApiBusinessRepository
    @Inject
    constructor(
        private val projectApi: ProjectApi,
    ) : BusinessRepository {
        private val latest = ConcurrentHashMap<String, BusinessInfo>()
        private val saved = MutableSharedFlow<String>(extraBufferCapacity = 1)

        /** Loads, then reloads after each save of this website. */
        override fun observeBusiness(websiteId: String): Flow<DataState<BusinessInfo>> =
            flow {
                emit(load(websiteId))
                saved.filter { it == websiteId }.collect { emit(load(websiteId)) }
            }

        override fun updateProfile(
            websiteId: String,
            draft: BusinessDraft,
        ): Flow<DataState<Unit>> = save(websiteId) { current -> draft to current.hours }

        override fun updateHours(
            websiteId: String,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> = save(websiteId) { current -> current.toDraft() to hours }

        override fun saveAll(
            websiteId: String,
            draft: BusinessDraft,
            hours: List<DayHours>,
        ): Flow<DataState<Unit>> = save(websiteId) { draft to hours }

        private fun save(
            websiteId: String,
            body: (current: BusinessInfo) -> Pair<BusinessDraft, List<DayHours>>,
        ): Flow<DataState<Unit>> =
            apiFlow {
                val current = latest[websiteId] ?: fetch(websiteId)
                val (draft, hours) = body(current)
                projectApi.saveBusinessInfo(websiteId, businessInfoBody(draft, hours))
                saved.tryEmit(websiteId)
            }

        private suspend fun load(websiteId: String): DataState<BusinessInfo> = loadState { fetch(websiteId) }

        private suspend fun fetch(websiteId: String): BusinessInfo =
            coroutineScope {
                val site = async { projectApi.website(websiteId) }
                val info = async { projectApi.businessInfo(websiteId) }
                info.await().toBusinessInfo(site.await()).also { latest[websiteId] = it }
            }
    }
