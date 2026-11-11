package com.kshrd.features.notifications.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/** The portal tab a notification can jump to (iOS `PortalTab`). */
enum class PortalTarget { Products, Info, Monitor }

@Immutable
data class AppNotification(
    val id: String,
    /** The website name, or "WebBlock ..." for messages from WebBlock itself. */
    val source: String,
    val message: String,
    /** Display text such as "2 mins ago" (sample data; a backend would send a timestamp). */
    val timeText: String,
    val unread: Boolean,
    @DrawableRes val logoRes: Int? = null,
    /** Longer explanation shown on the notification's own page. */
    val detail: String = "",
    /** Where "Open" takes the owner: a website and the tab inside its portal. */
    val websiteId: String? = null,
    val target: PortalTarget? = null,
    val actionTitle: String? = null,
) {
    /** Sent by WebBlock itself (billing, support, welcome) rather than by one of the owner's websites. */
    val isFromWebBlock: Boolean get() = source.startsWith("WebBlock")

    val initials: String
        get() =
            source
                .split(' ')
                .filter { it.isNotBlank() }
                .take(2)
                .mapNotNull { word -> word.firstOrNull { it.isLetter() } }
                .joinToString("") { it.uppercase() }
}
