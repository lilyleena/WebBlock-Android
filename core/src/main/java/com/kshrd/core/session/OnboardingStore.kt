package com.kshrd.core.session

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers which accounts have already seen the intro, so it only shows on an account's first login. */
@Singleton
class OnboardingStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun hasSeen(email: String): Boolean = key(email) in seenFor()

        fun markSeen(email: String) {
            prefs.edit().putStringSet(KEY_SEEN_FOR, seenFor() + key(email)).apply()
        }

        private fun seenFor(): Set<String> = prefs.getStringSet(KEY_SEEN_FOR, emptySet()).orEmpty()

        private fun key(email: String) = email.trim().lowercase()

        private companion object {
            const val PREFS_NAME = "webblock_onboarding"
            const val KEY_SEEN_FOR = "seenForAccounts"
        }
    }
