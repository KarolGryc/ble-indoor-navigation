package org.example.indoor.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import koin.createKoinConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import presentation.maplist.MapListScreen
import presentation.navigationScreen.BuildingMapScreen
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(KoinExperimentalAPI::class, ExperimentalUuidApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }

        val navController = rememberNavController()

        BindEffect(controller)

        KoinMultiplatformApplication(
            config = createKoinConfiguration(),
        ) {
            NavHost(
                navController = navController,
                startDestination = "mapList"
            ) {
                composable("mapList") {
                    MapListScreen(
                        koinViewModel(),
                        onNavigateToMap = { uuid ->
                            navController.navigate("details/$uuid")
                        }
                    )
                }

                composable(
                    route = "details/{mapId}",
                    arguments = listOf(navArgument("mapId") { type = NavType.StringType } )
                ) { backStackEntry ->
                    val mapIdString = backStackEntry.savedStateHandle.get<String>("mapId") ?: ""
                    val mapUuid = try {
                        Uuid.parse(mapIdString)
                    } catch (_: Exception) {
                        null
                    }

                    BuildingMapScreen(
                        viewModel = koinViewModel { parametersOf(mapUuid) },
                        onClickBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController,
    snackbarHostState: SnackbarHostState
) {
    if(!controller.isPermissionGranted(permission)) {
        try {
            controller.providePermission(permission)
        } catch (_: DeniedAlwaysException) {
            val result = snackbarHostState.showSnackbar(
                message = "Permanently Denied",
                actionLabel = "Open Settings",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }
        } catch (_: DeniedException) {
            snackbarHostState.showSnackbar(
                message = "Permission Denied",
                duration = SnackbarDuration.Short
            )
        } catch (_: RequestCanceledException) {
            snackbarHostState.showSnackbar(
                message = "Request canceled for permission: $permission"
            )
        }
    } else {
        snackbarHostState.showSnackbar(
            message = "Permission already granted: $permission"
        )
    }
}