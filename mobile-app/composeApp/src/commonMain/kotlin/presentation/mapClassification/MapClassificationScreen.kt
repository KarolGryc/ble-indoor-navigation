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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import presentation.composables.AcceptRejectDialog
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapClassificationScreen(
    viewModel: MapClassificationViewModel,
    onClickBack: () -> Unit
) {
    val vmState by viewModel.state.collectAsState()
    var saveRequested by remember { mutableStateOf(false) }
    var resetRequested by remember { mutableStateOf(false) }
    var unsavedDataWarning by remember { mutableStateOf(false) }
    var errorWhileLoading by remember { mutableStateOf(false) }

    if (vmState.currentStage is CalibrationStage.Error) {
        LaunchedEffect(vmState.currentStage) {
            when(vmState.currentStage) {
                is CalibrationStage.Error.BuildingLoadFailed -> "Failed while loading the map"
                else -> "Unknown error"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Classify map zones", style = MaterialTheme.typography.titleMedium)
                        Text(text = vmState.buildingName, style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if(vmState.unsavedData) unsavedDataWarning = true
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
            vmState.floors.forEach { floor ->
                item(key = "header_${floor.name}") {
                    FloorHeader(floorName = floor.name)
                }

                items(items = floor.zones, key = { it.id }) { zone ->
                    val asRecordingState = vmState.currentStage as? CalibrationStage.SignalsRecording

                    ZoneItem(
                        zone = zone,
                        isGlobalRecording = asRecordingState != null,
                        isRecorded = asRecordingState?.zoneUiItem?.id == zone.id,
                        progress = asRecordingState?.progress ?: 0f,
                        onRecordClick = { viewModel.recordData(zone.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        val resMeasurements = (vmState.currentStage as? CalibrationStage.Result)?.fingerprint?.measurements
        AcceptRejectDialog(
            show = vmState.currentStage is CalibrationStage.Result,
            titleText = "Accept this measurement?",
            dialogText = fingerprintAsString(vmState.currentStage),
            onReject = viewModel::resetCalibrationStage,
            onAccept = if(resMeasurements?.isNotEmpty() ?: false) viewModel::acceptPendingFingerprint else null
        )

        AcceptRejectDialog(
            show = saveRequested,
            titleText = "Saving config",
            dialogText = "Do you want to save the new config?",
            onAccept = { viewModel.persistBuildingConfig(); saveRequested = false },
            onReject = { saveRequested = false }
        )

        AcceptRejectDialog(
            show = unsavedDataWarning,
            titleText = "Unsaved progress!",
            dialogText = "Do you want to quit without saving?\nAll unsaved progress will be lost!!!",
            onAccept = { onClickBack(); },
            onReject = { unsavedDataWarning = false }
        )

        AcceptRejectDialog(
            show = resetRequested,
            titleText = "Reset of configuration",
            dialogText = "Do you want to reset ${vmState.buildingName} configuration?",
            onAccept = { viewModel.resetCalibration(); resetRequested = false  },
            onReject = { resetRequested = false }
        )

        AcceptRejectDialog(
            show = errorWhileLoading,
            titleText = "Loading map error!",
            dialogText = "Cannot load map ${vmState.buildingName} ☠\uFE0F",
            onAccept = { onClickBack() }
        )
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