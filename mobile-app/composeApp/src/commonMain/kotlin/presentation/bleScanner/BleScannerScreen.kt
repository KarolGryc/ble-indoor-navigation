package presentation.bleScanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import domain.repository.BleScanError
import kotlinx.coroutines.launch
import presentation.permissions.checkPermissions
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun BleScannerScreen(
    viewModel: BleScanViewModel,
    onBackPressed: () -> Unit
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val uiState by viewModel.uiState.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetShadowElevation = 8.dp,
        sheetPeekHeight = 120.dp,

        sheetContent = {
            BleScannerBottomSheet(
                controller = controller,
                uiState = uiState,
                onScanStarted = { viewModel.startScan() },
                onScanStopped = { viewModel.stopScan() },
                onFilterByNameChanged = { shouldFilter -> viewModel.filterByName = shouldFilter }
            )
        },

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search nearby BLE devices") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Detected: ${uiState.devices.size} device${if(uiState.devices.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isScanning || uiState.devices.isNotEmpty()) {
                    BleDevicesList(
                        devices = uiState.devices,
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Press start to search", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun BleScannerBottomSheet(
    controller: PermissionsController,
    uiState: BleScanUiState,
    onScanStarted: () -> Unit,
    onScanStopped: () -> Unit,
    onFilterByNameChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScanningEnableButton(
            controller = controller,
            uiState = uiState,
            onScanStarted = onScanStarted,
            onScanStopped = onScanStopped
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Other Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilteringToggleButton(onFilterByNameChanged = onFilterByNameChanged)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ScanningEnableButton(
    controller: PermissionsController,
    uiState: BleScanUiState,
    onScanStarted: () -> Unit,
    onScanStopped: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = {
            scope.launch {
                checkPermissions(
                    permissions = listOf(
                        Permission.LOCATION,
                        Permission.BLUETOOTH_SCAN
                    ),
                    controller = controller,
                    onGranted = {
                        if (uiState.isScanning) onScanStopped()
                        else onScanStarted()
                    }
                )
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if(uiState.error != BleScanError.None) {
                MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.primary,
        )
    ) {
        Icon(
            imageVector = if (uiState.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = when {
            uiState.isScanning -> "Stop Scanning"
            uiState.error is BleScanError.LocationPermissionDenied -> "Location Permission Denied"
            uiState.error is BleScanError.BluetoothDisabled-> "Enable Bluetooth to scan"
            else -> "Start Scanning"
        })
    }
}

@Composable
fun FilteringToggleButton(
    onFilterByNameChanged: (Boolean) -> Unit
) {
    var filterByName by remember { mutableStateOf(false) }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            filterByName = !filterByName
            onFilterByNameChanged(filterByName)
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filterByName) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (filterByName) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary
        ),
    ) {
        Icon(
            imageVector = if (filterByName) Icons.Default.Check else Icons.Default.FilterList,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = if (filterByName) "Filtering by name" else "Enable filtering by name")
    }
}