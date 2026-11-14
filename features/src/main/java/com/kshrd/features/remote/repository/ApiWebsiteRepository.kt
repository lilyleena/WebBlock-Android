package com.kshrd.features.remote.repository

import com.kshrd.core.api.apiFlow
import com.kshrd.core.data.entity.DataState
import com.kshrd.features.remote.api.ProjectApi
import com.kshrd.features.remote.api.ProvisioningApi
import com.kshrd.features.remote.dto.WebsiteDto
import com.kshrd.features.remote.dto.allPages
import com.kshrd.features.remote.mapper.toWebsite
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.repository.WebsiteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

private const val HTTP_NOT_FOUND = 404

/** Project v2 websites, with Provisioning's hosting state for the live ones (docs/API-MAPPING.md 2.2). */
class ApiWebsiteRepository
    @Inject
    constructor(
        private val projectApi: ProjectApi,
        private val provisioningApi: ProvisioningApi,
    ) : WebsiteRepository {
        /** The owner comes from the token; [ownerId] is only for the sample data. */
        override fun getWebsites(ownerId: String): Flow<DataState<List<Website>>> =
            apiFlow {
                val sites = allPages { page -> projectApi.websites(page) }
                coroutineScope {
                    sites.map { site -> async { site.withHosting() } }.awaitAll()
                }
            }

        /** A live site: hosting offline gets the "Offline" badge, and a missing logo falls back to business info (G4). */
        private suspend fun WebsiteDto.withHosting(): Website {
            if (status != "LIVE") return toWebsite()
            val offline = isHostingOffline(id)
            val logo = logoUrl ?: optional { projectApi.businessInfo(id).logoUrl }
            return copy(logoUrl = logo).toWebsite(hostingOffline = offline)
        }

        /** Never deployed (404 `deployment-not-found`) counts as offline; no answer at all is not a badge. */
        private suspend fun isHostingOffline(projectId: String): Boolean =
            attempt { provisioningApi.status(projectId) }
                .fold(onSuccess = { !it.online }, onFailure = { (it as? HttpException)?.code() == HTTP_NOT_FOUND })
    }
