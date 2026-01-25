package com.prashik.firewallapp.ui.bottomBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.prashik.firewallapp.ui.MainViewModel
import com.prashik.firewallapp.ui.screen.Select_Apps
import com.prashik.firewallapp.ui.screen.NetworkAppsScreen
import com.prashik.firewallapp.ui.screen.AppAddressScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val newBackStack = rememberNavBackStack<Screen>(Screen.NestedGraph)

    val viewModel: MainViewModel = koinViewModel()

    val switchState = viewModel.switchState.collectAsStateWithLifecycle()
    LaunchedEffect(switchState.value) {
        viewModel.getBlockLogs()
        viewModel.loadAppsWithAddresses()
    }
    val firewallTrafficLogs = viewModel.allBlackLogs.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = newBackStack,
        onBack = { newBackStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider {
            entry<Screen.SelectApps> {
                Select_Apps(
                    onNavBackClicked = {
                        newBackStack.removeLastOrNull()
                    },
                    viewModel = viewModel
                )
            }
            entry<Screen.NetworkApps> {
                val appsWithAddresses = viewModel.appsWithAddresses.collectAsStateWithLifecycle()
                NetworkAppsScreen(
                    apps = appsWithAddresses.value,
                    onAppClick = { packageName, appName ->
                        newBackStack.add(Screen.AppAddresses(packageName, appName))
                    }
                )
            }
            entry<Screen.AppAddresses> { entry ->
                val addresses = viewModel.currentAppAddresses.collectAsStateWithLifecycle()
                LaunchedEffect(entry.packageName) {
                    viewModel.loadAddressesForApp(entry.packageName)
                }
                AppAddressScreen(
                    appName = entry.appName,
                    packageName = entry.packageName,
                    addresses = addresses.value,
                    onBackClick = {
                        newBackStack.removeLastOrNull()
                    },
                    onToggleAddress = { addressId, isBlocked ->
                        viewModel.toggleAddressBlock(addressId, isBlocked)
                    },
                    onBlockAll = {
                        viewModel.blockAllAddressesForApp(entry.packageName)
                    },
                    onAllowAll = {
                        viewModel.allowAllAddressesForApp(entry.packageName)
                    }
                )
            }
            entry<Screen.NestedGraph> {
                Nested_Graph(
                    onFabClicked = { newBackStack.add(Screen.SelectApps) },
                    viewModel = viewModel,
                    switchState = switchState.value,
                    onSwitchStateChange = { switchState ->
                        viewModel.onSwitchChange(switchState)
                    },
                    firewallTrafficLogs = firewallTrafficLogs.value
                )
            }
        }
    )
}