package com.kshrd.core.auth

/** Where the session's tokens survive an app restart. */
interface TokenStore {
    /** The stored tokens, or `null` when there are none or they can't be read (then the owner signs in again). */
    fun load(): StoredTokens?

    fun save(tokens: StoredTokens)

    fun clear()
}
