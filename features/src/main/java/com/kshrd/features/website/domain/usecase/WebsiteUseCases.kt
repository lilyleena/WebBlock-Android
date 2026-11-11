package com.kshrd.features.website.domain.usecase

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.data.entity.asUseCaseFlow
import com.kshrd.core.data.entity.mapSuccess
import com.kshrd.core.session.SessionOwner
import com.kshrd.core.session.SessionStore
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.repository.WebsiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Loads the signed-in owner's live websites (the only ones with a portal) and publishes them to
 * the session, so the website switcher can list them without loading again.
 */
class GetWebsitesUseCase
    @Inject
    constructor(
        private val repository: WebsiteRepository,
        private val session: SessionStore,
    ) {
        operator fun invoke(): Flow<DataState<List<Website>>> {
            val owner = session.owner.value ?: return flowOf(DataState.Error(message = "Not signed in"))
            return repository
                .getWebsites(owner.id)
                .asUseCaseFlow()
                // Only live websites have a portal, so only those are ever shown or selectable.
                .map { it.mapSuccess { websites -> websites.filter { site -> site.hasPortal } } }
                .onEach { result ->
                    if (result is DataState.Success) {
                        session.setWebsites(result.data.map { WorkspaceWebsite(it.id, it.name, it.url.orEmpty()) })
                    }
                }
        }
    }

/** Remembers which website the owner opened. Everything in the portal reads it from the session. */
class OpenWebsiteUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke(websiteId: String) = session.selectWebsite(websiteId)
    }

class ObserveOwnerUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke(): StateFlow<SessionOwner?> = session.owner
    }

class SignOutUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke() = session.signOut()
    }
