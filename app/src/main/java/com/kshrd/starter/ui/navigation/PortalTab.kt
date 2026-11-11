package com.kshrd.starter.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kshrd.core.navigation.AppDestination
import com.kshrd.core.navigation.BusinessTab
import com.kshrd.core.navigation.MonitorTab
import com.kshrd.core.navigation.ProductsTab
import com.kshrd.coreui.components.nav.BottomBarItem
import com.kshrd.coreui.components.nav.PortalIcons
import com.kshrd.starter.R

/** The three sections of the website management portal, in bottom-bar order. */
enum class PortalTab(
    val destination: AppDestination,
    @StringRes val labelRes: Int,
) {
    Products(ProductsTab, R.string.tab_products),
    Business(BusinessTab, R.string.tab_business),
    Monitor(MonitorTab, R.string.tab_monitor),
}

@Composable
fun PortalTab.item(): BottomBarItem {
    val label = stringResource(labelRes)
    return when (this) {
        PortalTab.Products -> BottomBarItem(label, PortalIcons.Products, PortalIcons.ProductsSelected)
        PortalTab.Business -> BottomBarItem(label, PortalIcons.Business, PortalIcons.BusinessSelected)
        PortalTab.Monitor -> BottomBarItem(label, PortalIcons.Monitor, PortalIcons.MonitorSelected)
    }
}
