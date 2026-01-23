package com.prashik.firewallapp.ui.bottomBar

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen: NavKey {

    @Serializable
    data object SelectApps: Screen()

    @Serializable
    data object NestedGraph: Screen()

    @Serializable
    data object NetworkApps: Screen()

    @Serializable
    data class AppAddresses(val packageName: String, val appName: String): Screen()
}