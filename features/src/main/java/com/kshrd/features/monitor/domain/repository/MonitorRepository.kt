package com.kshrd.features.monitor.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.monitor.domain.model.MonitorData
import kotlinx.coroutines.flow.Flow

interface MonitorRepository {
    fun observeMonitor(websiteId: String): Flow<DataState<MonitorData>>
}
