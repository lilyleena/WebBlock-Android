package com.kshrd.features.website.data.source

import com.kshrd.features.mock.MockIds
import com.kshrd.features.R
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.model.WebsiteStatus
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** Mock websites. Ids are shared with the other in-memory sources so every screen lines up. */
@Singleton
class InMemoryWebsiteDataSource
    @Inject
    constructor() {
        private var errorOnce = true

        suspend fun websitesFor(ownerId: String): Result<List<Website>> {
            delay(LATENCY_MS)
            return when (ownerId) {
                MockIds.OWNER_EMPTY -> Result.success(emptyList())
                MockIds.OWNER_ERROR ->
                    if (errorOnce) {
                        errorOnce = false
                        Result.failure(IllegalStateException("We couldn't load your websites. Check your connection and try again."))
                    } else {
                        Result.success(alexWebsites())
                    }
                else -> Result.success(alexWebsites())
            }
        }

        private fun alexWebsites(): List<Website> =
            listOf(
                Website(MockIds.PETAL_STEM, "Petal & Stem", "Florist", WebsiteStatus.Live, "petalandstem.webblock.com", R.drawable.petal_stem_logo),
                Website(MockIds.GOLDEN_CRUMB, "Golden Crumb Bakery", "Bakery", WebsiteStatus.Live, "goldencrumb.webblock.com", R.drawable.golden_crumb_logo),
                Website(MockIds.BEAN_BREW, "Bean & Brew Café", "Café", WebsiteStatus.Live, "beanandbrew.webblock.com", R.drawable.bean_brew_logo),
                Website(MockIds.STUDIO_LOTUS, "Studio Lotus Yoga", "Yoga studio", WebsiteStatus.Live, "studiolotus.webblock.com", R.drawable.studio_lotus_logo),
                // Not live, so the app hides them (the "live only" rule).
                Website("maple-rye", "Maple & Rye Bakery", "Bakery", WebsiteStatus.Draft, null),
                Website("lantern-cafe", "Lantern Café", "Café", WebsiteStatus.Draft, "lanterncafe.webblock.com"),
                Website("northpoint-audio", "Northpoint Audio", "Audio store", WebsiteStatus.Suspended, "northpointaudio.webblock.com"),
            )

        companion object {
            private const val LATENCY_MS = 900L
        }
    }
