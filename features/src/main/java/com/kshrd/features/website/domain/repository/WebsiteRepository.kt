package com.kshrd.features.website.domain.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.features.website.domain.model.Website
import kotlinx.coroutines.flow.Flow

interface WebsiteRepository {
    /** All websites owned by [ownerId]. */
    fun getWebsites(ownerId: String): Flow<DataState<List<Website>>>
}
