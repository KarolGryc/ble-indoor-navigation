package org.example.indoor.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import data.dto.BuildingMapDto
import data.filesystemProviders.rememberFilePicker
import data.mapper.BuildingMapper
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import koin.createKoinConfiguration
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import presentation.maplist.MapListViewModel

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }

        BindEffect(controller)

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            KoinMultiplatformApplication(
                config = createKoinConfiguration(),
            ) {
//                var name by remember { mutableStateOf("No name selected") }
//                val picker = rememberFilePicker { file ->
//                    name = file?.content?.size?.toString() ?: "Failed to get file"
//                }
//
//                Column {
//                    Button(onClick = { picker.pickFile() }) {
//                        Text("Get file")
//                    }
//                    Button(onClick = {}) {
//                        Text(name)
//                    }
//                }
                val viewModel = koinViewModel<MapListViewModel>()
                val picker = rememberFilePicker { file ->
                    if (file != null) {
                        scope.launch {
                            try {
                                val jsonString = file.content.decodeToString()
                                val jsonParser = Json { ignoreUnknownKeys = true }
                                val dto = jsonParser.decodeFromString<BuildingMapDto>(jsonString)
                                val map = BuildingMapper.mapToDomain(dto)
                                viewModel.addMap(file.name.replace(".json", ""), map)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                snackbarHostState.showSnackbar(
                                    message = "Failed to parse file: ${e.message}"
                                )
                            }

                        }
                    }
                    else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Failed to get file"
                            )
                        }
                    }
                }


                val state by viewModel.uiState.collectAsState()
                Column {
                    Button(onClick = {
                        picker.pickFile()
                    }) {
                        Text("Add Map")
                    }

                    LazyColumn {
                        items(state.mapNames.size) { index ->
                            val name = viewModel.uiState.value.mapNames[index]
                            Text(text = name)
                        }
                    }
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