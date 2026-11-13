package com.kshrd.starter.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kshrd.core.navigation.BusinessTab
import com.kshrd.core.navigation.ForgotPassword
import com.kshrd.core.navigation.Login
import com.kshrd.core.navigation.MonitorTab
import com.kshrd.core.navigation.MyWebsites
import com.kshrd.core.navigation.NotificationDetail
import com.kshrd.core.navigation.Notifications
import com.kshrd.core.navigation.ProductDetail
import com.kshrd.core.navigation.ProductsTab
import com.kshrd.core.navigation.Onboarding
import com.kshrd.core.navigation.navigateTo
import com.kshrd.coreui.components.nav.AppBottomBar
import com.kshrd.coreui.motion.AppMotion
import com.kshrd.features.auth.presentation.screen.ForgotPasswordScreen
import com.kshrd.features.auth.presentation.screen.LoginScreen
import com.kshrd.features.auth.presentation.screen.OnboardingScreen
import com.kshrd.features.business.presentation.screen.BusinessScreen
import com.kshrd.features.monitor.presentation.screen.MonitorScreen
import com.kshrd.features.notifications.presentation.screen.NotificationDetailScreen
import com.kshrd.features.notifications.presentation.screen.NotificationsScreen
import com.kshrd.features.products.presentation.screen.ProductDetailScreen
import com.kshrd.features.products.presentation.screen.ProductsScreen
import com.kshrd.features.website.presentation.screen.MyWebsitesScreen

/**
 * Maps every typed destination in `:core` to the screen that renders it.
 * Feature screens receive navigation as callbacks; only this file knows the graph.
 *
 * Flow: (splash) -> Onboarding -> Login -> My Websites -> (select a website) -> portal tabs
 * (Products, Business, Monitor) with a shared bottom bar.
 */
/** Result key the forgot-password flow leaves on the sign-in entry after a successful reset. */
private const val PREFILL_EMAIL_KEY = "prefillEmail"

@Composable
fun MainGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = PortalTab.entries.firstOrNull { backStackEntry.isOn(it) }
    // The bottom bar shows on the three tabs only; a product's page has its own Edit / Delete bar instead.
    val barTab = currentTab
    val isAuth = backStackEntry.isOn<Onboarding>() || backStackEntry.isOn<Login>() || backStackEntry.isOn<ForgotPassword>()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (barTab != null) {
                AppBottomBar(
                    items = PortalTab.entries.map { it.item() },
                    selectedIndex = barTab.ordinal,
                    onSelect = { navController.openTab(PortalTab.entries[it]) },
                )
            }
        },
    ) { padding ->
        val insets = Modifier
        Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Onboarding,
            // On the three portal tabs the content runs underneath the floating glass bar; their
            // lists leave room at the bottom so the last item can scroll clear of it.
            modifier = (if (barTab != null) Modifier.padding(top = padding.calculateTopPadding()) else Modifier.padding(padding)).then(insets),
            enterTransition = { AppMotion.forwardEnter() },
            exitTransition = { AppMotion.forwardExit() },
            popEnterTransition = { AppMotion.backEnter() },
            popExitTransition = { AppMotion.backExit() },
        ) {
            authDestinations(navController)
            portalDestinations(navController)
        }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.authDestinations(navController: NavHostController) {
    composable<Onboarding> {
        OnboardingScreen(
            onFinished = {
                // Replayed from My Websites: just go back. On app launch: continue to sign-in.
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    navController.navigateTo(Login) { popUpTo<Onboarding> { inclusive = true } }
                }
            },
        )
    }
    composable<Login> { entry ->
        val handle = entry.savedStateHandle
        val prefill by handle.getStateFlow<String?>(PREFILL_EMAIL_KEY, null).collectAsStateWithLifecycle()
        LoginScreen(
            // The intro already played before sign-in (app launch), so go straight to My Websites.
            onSignedIn = { _ -> navController.navigateTo(MyWebsites) { popUpTo<Login> { inclusive = true } } },
            onForgotPassword = { email -> navController.navigateTo(ForgotPassword(email)) },
            prefillEmail = prefill,
            onPrefillConsumed = { handle[PREFILL_EMAIL_KEY] = null },
        )
    }
    composable<ForgotPassword> {
        ForgotPasswordScreen(
            onFinish = { changedEmail ->
                if (changedEmail != null) navController.previousBackStackEntry?.savedStateHandle?.set(PREFILL_EMAIL_KEY, changedEmail)
                navController.popBackStack()
            },
        )
    }
    composable<MyWebsites> {
        MyWebsitesScreen(
            onOpenPortal = { navController.navigateTo(ProductsTab) },
            onOpenNotifications = { navController.navigateTo(Notifications) },
            onSignedOut = { navController.navigateTo(Login) { popUpTo(navController.graph.id) { inclusive = true } } },
            onReplayIntro = { navController.navigateTo(Onboarding) },
        )
    }
}

private fun androidx.navigation.NavGraphBuilder.portalDestinations(navController: NavHostController) {
    val openNotifications = { navController.navigateTo(Notifications) }
    val backToWebsites = { navController.popBackStack<MyWebsites>(inclusive = false) }
    composable<ProductsTab> {
        ProductsScreen(
            onOpenProduct = { navController.navigateTo(ProductDetail(it)) },
            onOpenNotifications = openNotifications,
            onBackToWebsites = { backToWebsites() },
        )
    }
    composable<ProductDetail> { ProductDetailScreen(onBack = { navController.popBackStack() }) }
    composable<BusinessTab> {
        BusinessScreen(
            onEditInfo = {},
            onEditHours = {},
            onOpenNotifications = openNotifications,
            onBackToWebsites = { backToWebsites() },
        )
    }
    composable<MonitorTab> {
        MonitorScreen(onOpenNotifications = openNotifications, onBackToWebsites = { backToWebsites() })
    }
    composable<Notifications> {
        NotificationsScreen(
            onBack = { navController.popBackStack() },
            onOpenDetail = { navController.navigateTo(NotificationDetail(it)) },
        )
    }
    composable<NotificationDetail> {
        NotificationDetailScreen(onBack = { navController.popBackStack() })
    }
}

private fun NavHostController.openTab(tab: PortalTab) {
    navigate(tab.destination) {
        popUpTo(ProductsTab) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavBackStackEntry?.isOn(tab: PortalTab): Boolean = this?.destination?.hasRoute(tab.destination::class) == true

private inline fun <reified T : Any> NavBackStackEntry?.isOn(): Boolean = this?.destination?.hasRoute<T>() == true
