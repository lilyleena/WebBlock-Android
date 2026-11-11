package com.kshrd.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations. Every screen in the app is one of these.
 *
 * Destinations live in `:core` so any feature can navigate to any screen without depending on the
 * feature module that implements it. The graph that maps a destination to a composable lives in
 * `:app` (`MainGraph.kt`).
 *
 * - A screen without arguments is a `data object`.
 * - A screen with arguments is a `data class`; the ViewModel reads them with
 *   `savedStateHandle.toRoute<NoteDetail>()`.
 */
sealed interface AppDestination

@Serializable
data object NoteList : AppDestination

@Serializable
data class NoteDetail(val noteId: Long) : AppDestination

// WebBlock Owner app. Order follows the user flow: onboarding (first launch) -> login -> my websites -> portal.
@Serializable
data object Onboarding : AppDestination

@Serializable
data object Login : AppDestination

/** [email] is whatever the owner already typed on the sign-in screen. */
@Serializable
data class ForgotPassword(val email: String = "") : AppDestination

@Serializable
data object MyWebsites : AppDestination

/** The three bottom-navigation tabs of the website management portal. */
@Serializable
data object ProductsTab : AppDestination

@Serializable
data object BusinessTab : AppDestination

@Serializable
data object MonitorTab : AppDestination

@Serializable
data class ProductDetail(val productId: String) : AppDestination

@Serializable
data object EditBusiness : AppDestination

@Serializable
data object OpeningHours : AppDestination

@Serializable
data object Notifications : AppDestination

@Serializable
data class NotificationDetail(val notificationId: String) : AppDestination
