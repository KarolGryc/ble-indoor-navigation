@file:OptIn(ExperimentalTime::class)

package org.example.indoor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import koin.createKoinConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import presentation.bleScanner.BleScannerScreen
import presentation.maplist.MapListScreen
import presentation.navigationScreen.BuildingMapScreen
import presentation.theme.NaviTheme
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(KoinExperimentalAPI::class, ExperimentalUuidApi::class)
@Composable
@Preview
fun App() {
    NaviTheme {
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }

        val navController = rememberNavController()

        BindEffect(controller)

        KoinMultiplatformApplication(
            config = createKoinConfiguration(),
        ) {
            NavHost(navController = navController, startDestination = "mapList") {
                composable("mapList") {
                    MapListScreen(
                        koinViewModel(),
                        onNavigateToMap = { uuid ->
                            navController.navigate("details/$uuid")
                        },
                        onBluetoothSearchPressed = { navController.navigate("bleScanner") }
                    )
                }

                composable(
                    route = "details/{mapId}",
                    arguments = listOf(navArgument("mapId") { type = NavType.StringType } )
                ) { backStackEntry ->
                    val mapIdString = backStackEntry.savedStateHandle.get<String>("mapId") ?: ""
                    val mapUuid = try { Uuid.parse(mapIdString) } catch (_: Exception) { null }

                    BuildingMapScreen(
                        viewModel = koinViewModel { parametersOf(mapUuid) },
                        onClickBack = { navController.popBackStack() }
                    )
                }

                composable(route = "bleScanner") {
                    BleScannerScreen(
                        viewModel = koinViewModel(),
                        onBackPressed = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}