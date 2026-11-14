package com.kshrd.features.remote.mapper

import com.kshrd.features.remote.dto.WebsiteDto
import com.kshrd.features.website.domain.model.Website
import com.kshrd.features.website.domain.model.WebsiteStatus

/** docs/API-MAPPING.md 2.2. v2 has no failed state; anything unknown counts as a draft (no portal). */
fun WebsiteDto.toWebsite(hostingOffline: Boolean = false): Website =
    Website(
        id = id,
        name = name,
        businessType = businessType?.takeIf { it.isNotBlank() } ?: "—",
        status =
            when (status) {
                "LIVE" -> WebsiteStatus.Live
                "SUSPENDED" -> WebsiteStatus.Suspended
                else -> WebsiteStatus.Draft
            },
        url = domain,
        logoUrl = logoUrl,
        isHostingOffline = hostingOffline,
    )
