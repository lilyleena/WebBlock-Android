package com.kshrd.features.monitor.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.monitor.data.source.InMemoryMonitorDataSource
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.repository.MonitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MonitorRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryMonitorDataSource,
    ) : MonitorRepository {
        override fun observeMonitor(websiteId: String): Flow<DataState<MonitorData>> = flow { emit(DataState.Success(source.monitorFor(websiteId))) }
    }
