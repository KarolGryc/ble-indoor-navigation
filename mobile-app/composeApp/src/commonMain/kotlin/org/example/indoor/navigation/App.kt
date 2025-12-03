package org.example.indoor.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import data.filesystemProviders.rememberFilePicker
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
import org.koin.core.annotation.KoinExperimentalAPI

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
                var name by remember { mutableStateOf("No name selected") }
                val picker = rememberFilePicker { file ->
                    name = file?.name ?: "Name is null, you've failed!"
                }

                Column {
                    Button(onClick = { picker.pickFile() }) {
                        Text("Get file")
                    }
                    Button(onClick = {}) {
                        Text(name)
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
    val granted = controller.isPermissionGranted(permission)
    if(!granted) {
        try {
            controller.providePermission(permission)
        } catch (e: DeniedAlwaysException) {
            val result = snackbarHostState.showSnackbar(
                message = "Permanently Denied",
                actionLabel = "Open Settings",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }
        } catch (e: DeniedException) {
            val result = snackbarHostState.showSnackbar(
                message = "Permanently Denied",
                duration = SnackbarDuration.Short
            )
        } catch (e: RequestCanceledException) {
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