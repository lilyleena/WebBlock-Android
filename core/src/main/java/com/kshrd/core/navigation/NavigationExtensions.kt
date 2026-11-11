package com.kshrd.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/** Navigate to a typed [AppDestination]. Prefer this over string routes. */
inline fun <reified T : AppDestination> NavController.navigateTo(
    destination: T,
    noinline builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(destination, builder)
}
