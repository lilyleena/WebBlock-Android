package com.kshrd.features.remote.mapper

import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.domain.model.PortalTarget
import com.kshrd.features.remote.dto.NotificationDto

/**
 * docs/API-MAPPING.md 2.3. "Open" follows `target` (G5). A server without `target` (older than Notification's G5
 * deploy) opens a website's notification on its Monitor; a platform message has no button.
 */
fun NotificationDto.toAppNotification(
    timeText: String,
    logoUrl: String?,
): AppNotification {
    val siteId = target?.websiteId ?: websiteId
    val tab =
        when {
            siteId == null -> null
            target == null -> PortalTarget.Monitor
            else -> portalTargetOf(target.tab)
        }
    return AppNotification(
        id = id,
        source = title,
        message = message,
        timeText = timeText,
        unread = !read,
        detail = detail.orEmpty(),
        websiteId = siteId,
        target = tab,
        actionTitle = tab?.let(::actionTitleOf),
        logoUrl = logoUrl,
    )
}

internal fun portalTargetOf(tab: String?): PortalTarget? =
    when (tab) {
        "MONITOR" -> PortalTarget.Monitor
        "PRODUCTS" -> PortalTarget.Products
        "BUSINESS_INFO" -> PortalTarget.Info
        else -> null
    }

internal fun actionTitleOf(target: PortalTarget): String =
    when (target) {
        PortalTarget.Monitor -> "Open Monitor"
        PortalTarget.Products -> "Open Products"
        PortalTarget.Info -> "Open Business information"
    }
