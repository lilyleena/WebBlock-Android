package com.kshrd.features.monitor.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.repository.MonitorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMonitorUseCase
    @Inject
    constructor(
        private val repository: MonitorRepository,
    ) {
        operator fun invoke(websiteId: String): Flow<DataState<MonitorData>> = repository.observeMonitor(websiteId).asUseCaseFlow()
    }
