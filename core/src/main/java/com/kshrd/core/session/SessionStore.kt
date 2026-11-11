package com.kshrd.core.session

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** The signed-in owner. */
@Immutable
data class SessionOwner(
    val id: String,
    val name: String,
    val email: String,
)

/** The slice of a website every management screen needs to know about. */
@Immutable
data class WorkspaceWebsite(
    val id: String,
    val name: String,
    val domain: String,
) {
    /** Up to two letters for avatar tiles: "Coffee House" -> "CH". */
    val initials: String
        get() =
            name
                .split(' ')
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
}

/** All websites the owner can switch between, and the one currently being managed. */
@Immutable
data class WorkspaceState(
    val current: WorkspaceWebsite? = null,
    val websites: List<WorkspaceWebsite> = emptyList(),
)

/**
 * App-wide, in-memory session: who is signed in and which website is being managed.
 *
 * Every management screen derives its data from [workspace], so selecting or switching a website
 * here updates all of them. When a real backend exists, the token lives in `core/network` and this
 * store keeps only the selection.
 */
@Singleton
class SessionStore
    @Inject
    constructor() {
        private val _owner = MutableStateFlow<SessionOwner?>(null)
        val owner: StateFlow<SessionOwner?> = _owner.asStateFlow()

        private val _workspace = MutableStateFlow(WorkspaceState())
        val workspace: StateFlow<WorkspaceState> = _workspace.asStateFlow()

        fun signIn(owner: SessionOwner) {
            _owner.value = owner
            _workspace.value = WorkspaceState()
        }

        fun signOut() {
            _owner.value = null
            _workspace.value = WorkspaceState()
        }

        /** Called when the owner's website list has been loaded. Keeps the selection if it still exists. */
        fun setWebsites(websites: List<WorkspaceWebsite>) {
            _workspace.update { state ->
                state.copy(websites = websites, current = websites.firstOrNull { it.id == state.current?.id })
            }
        }

        fun selectWebsite(id: String) {
            _workspace.update { state -> state.copy(current = state.websites.firstOrNull { it.id == id } ?: state.current) }
        }

        fun clearSelection() {
            _workspace.update { it.copy(current = null) }
        }
    }
