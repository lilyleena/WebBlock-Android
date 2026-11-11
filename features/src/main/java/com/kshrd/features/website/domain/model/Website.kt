package com.kshrd.features.website.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/** Publication state. Only [Live] websites have a portal, so only they appear in the app. */
enum class WebsiteStatus { Live, Draft, Suspended }

@Immutable
data class Website(
    val id: String,
    val name: String,
    val businessType: String,
    val status: WebsiteStatus,
    val url: String?,
    /** Bundled logo; `null` falls back to the initials tile. Becomes a remote URL once a backend exists. */
    @DrawableRes val logoRes: Int? = null,
) {
    /** Only live websites have a management portal in the app. */
    val hasPortal: Boolean get() = status == WebsiteStatus.Live

    val initials: String
        get() =
            name
                .split(' ')
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
}
