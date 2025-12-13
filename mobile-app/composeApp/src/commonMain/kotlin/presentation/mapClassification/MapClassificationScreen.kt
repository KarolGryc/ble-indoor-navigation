package presentation.mapClassification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import domain.model.ErrorMessage
import kotlinx.coroutines.launch
import presentation.composables.AcceptRejectDialog
import presentation.composables.DialogOption
import presentation.composables.DialogWithOptions
import presentation.permissions.PermissionCheckResult
import presentation.permissions.checkPermissions
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapClassificationScreen(
    viewModel: MapClassificationViewModel,
    onClickBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val uiState by viewModel.state.collectAsState()
    var saveRequested by remember { mutableStateOf(false) }
    var resetRequested by remember { mutableStateOf(false) }
    var unsavedDataWarning by remember { mutableStateOf(false) }
    var errorWhileLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Classify map zones", style = MaterialTheme.typography.titleMedium)
                        Text(text = uiState.buildingName, style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if(uiState.unsavedData) unsavedDataWarning = true
                        else onClickBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = {
                            resetRequested = true
                        }) {
                            Icon(Icons.Default.Replay, "Reset")
                        }
                        IconButton(onClick = {
                            saveRequested = true
                        }) {
                            Icon(Icons.Default.Save, "Save")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.floors.forEach { floor ->
                item(key = "header_${floor.name}") {
                    FloorHeader(floorName = floor.name)
                }

                items(items = floor.zones, key = { it.id }) { zone ->
                    val asRecordingState = uiState.currentStage as? CalibrationStage.SignalsRecording

                    ZoneItem(
                        zone = zone,
                        isGlobalRecording = asRecordingState != null,
                        isRecorded = asRecordingState?.zoneUiItem?.id == zone.id,
                        progress = asRecordingState?.progress ?: 0f,
                        onRecordClick = {
                            scope.launch {
                                val permissionResult = checkPermissions(
                                    permissions = listOf(Permission.LOCATION, Permission.BLUETOOTH_SCAN),
                                    controller = controller
                                )

                                when (permissionResult) {
                                    PermissionCheckResult.Granted -> viewModel.recordData(zone.id)
                                    PermissionCheckResult.PermanentlyDenied ->
                                        viewModel.setErrorMessage(
                                            ErrorMessage(
                                                "Permissions Denied",
                                                "Location and Bluetooth permissions are permanently denied. Please enable them in settings."
                                            )
                                        )
                                    else -> {}
                                }
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        val resMeasurements = (uiState.currentStage as? CalibrationStage.Result)?.fingerprint?.measurements
        AcceptRejectDialog(
            show = uiState.currentStage is CalibrationStage.Result,
            title = "Accept this measurement?",
            message = fingerprintAsString(uiState.currentStage),
            onReject = viewModel::resetCalibrationStage,
            onAccept = if(resMeasurements?.isNotEmpty() ?: false) viewModel::acceptPendingFingerprint else null
        )

        AcceptRejectDialog(
            show = saveRequested,
            title = "Saving config",
            message = "Do you want to save the new config?",
            onAccept = { viewModel.persistBuildingConfig(); saveRequested = false },
            onReject = { saveRequested = false }
        )

        AcceptRejectDialog(
            show = unsavedDataWarning,
            title = "Unsaved progress!",
            message = "Do you want to quit without saving?\nAll unsaved progress will be lost!!!",
            onAccept = { onClickBack(); },
            onReject = { unsavedDataWarning = false }
        )

        AcceptRejectDialog(
            show = resetRequested,
            title = "Reset of configuration",
            message = "Do you want to reset ${uiState.buildingName} configuration?",
            onAccept = { viewModel.resetCalibration(); resetRequested = false  },
            onReject = { resetRequested = false }
        )

        AcceptRejectDialog(
            show = errorWhileLoading,
            title = "Loading map error!",
            message = "Cannot load map ${uiState.buildingName} ☠\uFE0F",
            onAccept = { onClickBack() }
        )

        if (uiState.errorMessage != null) {
            DialogWithOptions(
                title = uiState.errorMessage?.title ?: "",
                message = uiState.errorMessage?.message ?: "",
                options = listOf(
                    DialogOption(text = "OK", onClick = { viewModel.clearErrorMessage() })
                ),
                onDismiss = { viewModel.clearErrorMessage() }
            )
        }
    }
}

private fun fingerprintAsString(calibrationStage: CalibrationStage): String {
    return (calibrationStage as? CalibrationStage.Result)?.let {
        val lines = it.fingerprint.measurements.map { measurement ->
            "Id:${measurement.tagId}\tRssi: ${measurement.rssi} dBm"
        }

        if (lines.isEmpty()) "No signals found \uD83D\uDE1E" else lines.joinToString("\n")
    } ?: ""
}