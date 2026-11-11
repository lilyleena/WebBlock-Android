package com.kshrd.core.session

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Implemented by the notifications feature, consumed by every screen with a bell icon.
 * Lives in `:core` so features never depend on each other.
 */
interface UnreadCountSource {
    val unreadCount: Flow<Int>
}

/** What the top of every portal screen shows: the active website, the others, and the bell dot. */
@Immutable
data class PortalChrome(
    val workspace: WorkspaceState = WorkspaceState(),
    val unreadCount: Int = 0,
    val ownerName: String = "",
)

class ObservePortalChromeUseCase
    @Inject
    constructor(
        private val session: SessionStore,
        private val unread: UnreadCountSource,
    ) {
        operator fun invoke(): Flow<PortalChrome> =
            combine(session.workspace, unread.unreadCount, session.owner) { workspace, count, owner ->
                PortalChrome(workspace, count, owner?.name.orEmpty())
            }
    }

class SwitchWebsiteUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke(websiteId: String) = session.selectWebsite(websiteId)
    }

class ObserveWorkspaceUseCase
    @Inject
    constructor(
        private val session: SessionStore,
    ) {
        operator fun invoke(): Flow<WorkspaceState> = session.workspace
    }
