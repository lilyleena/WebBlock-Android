package com.kshrd.features.remote.mapper

import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.BusinessInfo
import com.kshrd.features.business.domain.model.BusinessProfile
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.remote.dto.BusinessInfoBody
import com.kshrd.features.remote.dto.BusinessInfoDto
import com.kshrd.features.remote.dto.OpeningHourDto
import com.kshrd.features.remote.dto.WebsiteDto
import java.time.DayOfWeek
import java.util.Locale

private const val MINUTES_PER_HOUR = 60
private const val DEFAULT_OPENS = 9 * MINUTES_PER_HOUR
private const val DEFAULT_CLOSES = 18 * MINUTES_PER_HOUR

/**
 * docs/API-MAPPING.md 2.5. Name, type and address come from the website (read-only in the app).
 * A weekday the server doesn't list is closed, 09:00–18:00, as on iOS.
 */
fun BusinessInfoDto.toBusinessInfo(website: WebsiteDto): BusinessInfo =
    BusinessInfo(
        profile =
            BusinessProfile(
                name = website.name,
                type = website.businessType?.takeIf { it.isNotBlank() } ?: "—",
                url = website.domain.orEmpty(),
                phone = phone,
                email = email,
                address = address,
                mapsLink = mapsUrl.orEmpty(),
                about = description,
            ),
        hours =
            DayOfWeek.entries.map { day ->
                val hour = hours.firstOrNull { it.day == day.name }
                DayHours(
                    day = day,
                    isOpen = hour?.open ?: false,
                    openMinutes = hour?.opensAt?.let(::minutesOf) ?: DEFAULT_OPENS,
                    closeMinutes = hour?.closesAt?.let(::minutesOf) ?: DEFAULT_CLOSES,
                )
            },
    )

/** `PUT /business-info`: all seven days, an empty maps link as `null`, the logo left out (kept). */
fun businessInfoBody(
    draft: BusinessDraft,
    hours: List<DayHours>,
): BusinessInfoBody =
    BusinessInfoBody(
        description = draft.about,
        phone = draft.phone,
        email = draft.email,
        address = draft.address,
        mapsUrl = draft.mapsLink.trim().ifEmpty { null },
        hours =
            DayOfWeek.entries.map { day ->
                val hour = hours.firstOrNull { it.day == day }
                OpeningHourDto(
                    day = day.name,
                    open = hour?.isOpen ?: false,
                    opensAt = hhmm(hour?.openMinutes ?: DEFAULT_OPENS),
                    closesAt = hhmm(hour?.closeMinutes ?: DEFAULT_CLOSES),
                )
            },
    )

/** "09:30" → 570; `null` when unreadable. */
internal fun minutesOf(text: String): Int? {
    val parts = text.split(':')
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hours * MINUTES_PER_HOUR + minutes
}

/** 570 → "09:30". */
internal fun hhmm(minutes: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)

/** The current info as a draft, for the save calls that change only one half of the PUT body. */
fun BusinessInfo.toDraft(): BusinessDraft =
    BusinessDraft(
        phone = profile.phone,
        email = profile.email,
        address = profile.address,
        mapsLink = profile.mapsLink,
        about = profile.about,
    )
