package com.kshrd.features.business.data.source

import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessProfile
import com.kshrd.features.business.domain.model.DayHours
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryBusinessDataSource
    @Inject
    constructor() {
        private val infos = mutableMapOf<String, MutableStateFlow<BusinessInfo>>()

        fun info(websiteId: String): StateFlow<BusinessInfo> = flowFor(websiteId).asStateFlow()

        suspend fun updateProfile(
            websiteId: String,
            draft: BusinessDraft,
        ) {
            delay(LATENCY_MS)
            flowFor(websiteId).update {
                it.copy(
                    profile =
                        it.profile.copy(
                            phone = draft.phone.trim(),
                            email = draft.email.trim(),
                            address = draft.address.trim(),
                            mapsLink = draft.mapsLink.trim(),
                            about = draft.about.trim(),
                        ),
                )
            }
        }

        suspend fun updateHours(
            websiteId: String,
            hours: List<DayHours>,
        ) {
            delay(LATENCY_MS)
            flowFor(websiteId).update { it.copy(hours = hours) }
        }

        private fun flowFor(websiteId: String) = infos.getOrPut(websiteId) { MutableStateFlow(seed(websiteId)) }

        /** Mirrors the iOS sample data. Unknown ids fall back to Petal & Stem, like the iOS service. */
        private fun seed(websiteId: String): BusinessInfo {
            val id = websiteId.lowercase()
            return when {
                "bakery" in id || "crumb" in id ->
                    BusinessInfo(
                        BusinessProfile(
                            name = "Golden Crumb Bakery",
                            type = "Bakery",
                            url = "goldencrumb.webblock.com",
                            phone = "023 456 789",
                            email = "hello@goldencrumb.com",
                            address = "42 Riverside Road, Phnom Penh",
                            mapsLink = "https://maps.app.goo.gl/GoldenCrumb42",
                            about = "Neighbourhood bakery making croissants, sourdough and cakes from scratch every morning.",
                        ),
                        week(open = 6, close = 16),
                    )
                "coffee" in id || "bean" in id || "brew" in id ->
                    BusinessInfo(
                        BusinessProfile(
                            name = "Bean & Brew Café",
                            type = "Café",
                            url = "beanandbrew.webblock.com",
                            phone = "077 123 456",
                            email = "hello@beanandbrew.com",
                            address = "9 Street 240, Phnom Penh",
                            mapsLink = "",
                            about = "Specialty coffee, fresh bakes and a quiet corner to work from, roasted locally.",
                        ),
                        week(open = 7, close = 20),
                    )
                "yoga" in id || "lotus" in id ->
                    BusinessInfo(
                        BusinessProfile(
                            name = "Studio Lotus Yoga",
                            type = "Yoga studio",
                            url = "studiolotus.webblock.com",
                            phone = "096 987 654",
                            email = "namaste@studiolotus.com",
                            address = "27 Sothearos Blvd, Phnom Penh",
                            mapsLink = "https://maps.app.goo.gl/StudioLotus27",
                            about = "Small-group vinyasa, yin and beginner classes in a calm, light-filled studio.",
                        ),
                        week(open = 7, close = 19, closed = setOf(DayOfWeek.SUNDAY)),
                    )
                else ->
                    BusinessInfo(
                        BusinessProfile(
                            name = "Petal & Stem",
                            type = "Florist",
                            url = "petalandstem.webblock.com",
                            phone = "012 345 678",
                            email = "hello@petalandstem.com",
                            address = "18 Garden Lane, Phnom Penh",
                            mapsLink = "https://maps.app.goo.gl/PetalStem18",
                            about = "Hand-tied bouquets, single stems and event flowers, arranged in our studio and delivered across town.",
                        ),
                        // iOS `OpeningDay.defaultWeek`: Monday to Friday, 9 AM to 6 PM.
                        week(open = 9, close = 18, closed = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
                    )
            }
        }

        private fun week(
            open: Int,
            close: Int,
            closed: Set<DayOfWeek> = emptySet(),
        ): List<DayHours> = DayOfWeek.entries.map { day -> DayHours(day, day !in closed, open * MINUTES_PER_HOUR, close * MINUTES_PER_HOUR) }

        private companion object {
            const val LATENCY_MS = 500L
            const val MINUTES_PER_HOUR = 60
        }
    }
