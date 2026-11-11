package com.kshrd.features.business.domain.model

import androidx.compose.runtime.Immutable
import java.time.DayOfWeek

/** Name, type and web address are set on the web and read-only in the app, as in the design. */
@Immutable
data class BusinessProfile(
    val name: String,
    val type: String,
    /** Public web address, e.g. `petalandstem.webblock.com`. */
    val url: String,
    val phone: String,
    val email: String,
    val address: String,
    val mapsLink: String,
    val about: String,
)

/** Opening hours of one weekday; times are minutes after midnight. */
@Immutable
data class DayHours(
    val day: DayOfWeek,
    val isOpen: Boolean,
    val openMinutes: Int,
    val closeMinutes: Int,
)

@Immutable
data class BusinessInfo(
    val profile: BusinessProfile,
    val hours: List<DayHours>,
)

/** The editable part of [BusinessProfile]. */
@Immutable
data class BusinessDraft(
    val phone: String,
    val email: String,
    val address: String,
    val mapsLink: String,
    val about: String,
)

enum class BusinessFormError { PhoneEmpty, EmailInvalid, AddressEmpty }

/** First problem found when saving the whole business form (checked in this order). */
enum class BusinessSaveIssue { AboutEmpty, PhoneEmpty, EmailInvalid }
