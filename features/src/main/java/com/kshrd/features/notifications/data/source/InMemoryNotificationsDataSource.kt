package com.kshrd.features.notifications.data.source

import com.kshrd.features.R
import com.kshrd.features.mock.MockIds
import com.kshrd.features.notifications.domain.model.AppNotification
import com.kshrd.features.notifications.domain.model.PortalTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Mock notification feed (same texts as the iOS sample). Holds read/unread state so the bell dot stays in sync. */
@Singleton
class InMemoryNotificationsDataSource
    @Inject
    constructor() {
        private val _items = MutableStateFlow(seed())
        val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

        fun markRead(id: String) = _items.update { list -> list.map { if (it.id == id) it.copy(unread = false) else it } }

        fun markAllRead() = _items.update { list -> list.map { it.copy(unread = false) } }

        @Suppress("LongMethod")
        private fun seed(): List<AppNotification> =
            listOf(
                AppNotification(
                    id = "n1",
                    source = "Petal & Stem",
                    message = "Your latest changes are live.",
                    timeText = "2 mins ago",
                    unread = true,
                    logoRes = R.drawable.petal_stem_logo,
                    detail = "Deploy 10 finished successfully and your latest changes are now live on petalandstem.webblock.com. Visitors see the updated products and opening hours.",
                    websiteId = MockIds.PETAL_STEM,
                    target = PortalTarget.Monitor,
                    actionTitle = "Open Monitor",
                ),
                AppNotification(
                    id = "n2",
                    source = "Northpoint Audio",
                    message = "Website suspended · reactivate it any time.",
                    timeText = "1 hour ago",
                    unread = true,
                    detail = "This website is suspended, so it is hidden from visitors and can’t be managed in the app. You can reactivate it any time from the WebBlock website.",
                ),
                AppNotification(
                    id = "n3",
                    source = "WebBlock Billing",
                    message = "Your free trial ends in 9 days. Pick a plan.",
                    timeText = "3 hours ago",
                    unread = true,
                    detail = "Your free trial ends in 9 days. Choose Plus, Pro or Ultra to keep your websites online and your products published.",
                    websiteId = MockIds.PETAL_STEM,
                    target = PortalTarget.Monitor,
                    actionTitle = "See plan usage",
                ),
                AppNotification(
                    id = "n4",
                    source = "WebBlock Support",
                    message = "We replied to your message about your domain.",
                    timeText = "5 hours ago",
                    unread = true,
                    detail = "Our team replied to your message about connecting your domain. Check your email for the full answer and the next steps.",
                ),
                AppNotification(
                    id = "n5",
                    source = "Lantern Café",
                    message = "Draft saved · publish when you’re ready.",
                    timeText = "Yesterday",
                    unread = false,
                    detail = "Your draft is saved. It isn’t live yet, so visitors can’t see it. Publish it from the WebBlock website when you’re ready.",
                ),
                AppNotification(
                    id = "n6",
                    source = "Maple & Rye",
                    message = "Website created from the Sweet pastel template.",
                    timeText = "2 days ago",
                    unread = false,
                    detail = "Your website was created from the Sweet pastel template. Add your products and details, then publish it.",
                ),
                AppNotification(
                    id = "n7",
                    source = "Petal & Stem",
                    message = "Connected to petalandstem.webblock.com.",
                    timeText = "2 days ago",
                    unread = false,
                    logoRes = R.drawable.petal_stem_logo,
                    detail = "Your website is now connected to petalandstem.webblock.com. Review how your details appear to customers in Business information.",
                    websiteId = MockIds.PETAL_STEM,
                    target = PortalTarget.Info,
                    actionTitle = "Open Business information",
                ),
                AppNotification(
                    id = "n8",
                    source = "WebBlock",
                    message = "Welcome to WebBlock, Sovannarith!",
                    timeText = "3 days ago",
                    unread = false,
                    detail = "Thanks for joining WebBlock. Pick a template, add your details and publish your first website in minutes.",
                ),
            )
    }
