package com.kshrd.features.website.data.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.toDataState
import com.kshrd.features.website.data.source.InMemoryWebsiteDataSource
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.repository.WebsiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WebsiteRepositoryImpl
    @Inject
    constructor(
        private val source: InMemoryWebsiteDataSource,
    ) : WebsiteRepository {
        override fun getWebsites(ownerId: String): Flow<DataState<List<Website>>> = flow { emit(source.websitesFor(ownerId).toDataState()) }
    }
