package org.example.indoor.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun BleSimpleScreen(
    viewModel: BleSimpleViewModel = koinViewModel()
) {
    val devices by viewModel.currentBleDevices.collectAsState()
    val numOfScans by viewModel.numOfScans.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Button(onClick = { viewModel.startScan() }) {
            Text("Start scanning", color = Color.Black)
        }

        Text(
            "Found devices: $numOfScans",
            color = Color.Black
        )

        devices.forEach { device ->
            Button(onClick = {}){
                Text(
                    "Device: ${device.name} - ${device.identifier} - RSSI: ${device.rssi}",
                    color = Color.Black
                )
            }
        }
    }
}

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
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Button(onClick = {
                    scope.launch {
                        checkPermission(
                            permission = Permission.LOCATION,
                            controller = controller,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }) {
                    Text("Get all permissions")
                }
                    BleSimpleScreen()
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