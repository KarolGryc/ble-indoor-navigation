package presentation.mapList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import data.filesystemProviders.File
import data.filesystemProviders.rememberFilePicker
import data.service.rememberFileSharer
import domain.repository.MapInfo
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import presentation.composables.AcceptRejectDialog
import presentation.composables.FloatingActions
import presentation.composables.GeneralAction
import presentation.composables.MapAction
import presentation.composables.MapList
import presentation.composables.TextInputDialog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Screen displaying a list of maps. Provides options to add new maps and manage existing ones.
 *
 * @param viewModel The ViewModel managing the map list state.
 * @param onNavigateToMap Callback invoked when a map is selected, providing the map's UUID.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapListScreen(
    viewModel: MapListViewModel = koinViewModel(),
    initialFile: File? = null,
    onNavigateToMap: (Uuid) -> Unit = { },
    onClassifyMap: (Uuid) -> Unit = {},
    onBluetoothSearchPressed: () -> Unit = { },
    onFileHandled: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var pendingFile by remember {  mutableStateOf(initialFile) }
    val filePicker = rememberFilePicker { pendingFile = it }

    val fileSharer = rememberFileSharer()
    val scope = rememberCoroutineScope()

    var renamedMapInfo by remember { mutableStateOf<MapInfo?>(null) }
    var removeMapRequested by remember { mutableStateOf<MapInfo?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Maps Menu") })
        },
        floatingActionButton = {
            FloatingActions(
                actions = listOf(
                    GeneralAction("Add from file", Icons.Default.Add) { filePicker.pickFile() },
                    GeneralAction("Search BLE devices", Icons.Default.SettingsBluetooth) { onBluetoothSearchPressed() }
                )
            )
       }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MapList(
                mapInfos = uiState.mapData,
                onMapSelected = onNavigateToMap,
                actions = listOf(
                    MapAction(
                        label = "Classify",
                        icon = Icons.Default.Settings,
                        onClick = { info -> onClassifyMap(info.id) }
                    ),
                    MapAction(
                        label = "Rename",
                        icon = Icons.Default.Edit,
                        onClick = { info -> renamedMapInfo = info }
                    ),
                    MapAction(
                        label = "Share",
                        icon = Icons.Default.Share,
                        onClick = { info ->
                            scope.launch {
                                viewModel.prepareBuildingToExport(info.id)?.let { file ->
                                    fileSharer.shareFile(file)
                                }
                            }
                        }
                    ),
                    MapAction(
                        label = "Delete",
                        icon = Icons.Default.Delete,
                        onClick = { info -> removeMapRequested = info }
                    )
                ),
                modifier = Modifier.weight(1f)
            )
        }

        if (pendingFile != null) {
            TextInputDialog(
                title = "Name the added map",
                onDismiss = {
                    pendingFile = null
                    onFileHandled()
                },
                onConfirm = { name ->
                    pendingFile?.let {
                        viewModel.addMap(name, it.content)
                        pendingFile = null
                        onFileHandled()
                    }
                },
                initialValue = pendingFile?.name
            )
        }
        if (renamedMapInfo != null) {
            TextInputDialog(
                title = "Rename the map",
                onDismiss = { renamedMapInfo = null },
                onConfirm = { name ->
                    renamedMapInfo?.let {
                        viewModel.renameMap(it.id, name)
                        renamedMapInfo = null
                    }
                },
                initialValue = renamedMapInfo?.name
            )
        }

        AcceptRejectDialog(
            show = removeMapRequested != null,
            title = "Delete map",
            message = "Are you sure you want to delete the map \"${removeMapRequested?.name}\"?",
            onAccept = {
                removeMapRequested?.let {
                    viewModel.removeMap(it.id)
                    removeMapRequested = null
                }
            },
            onReject = { removeMapRequested = null }
        )
    }
}