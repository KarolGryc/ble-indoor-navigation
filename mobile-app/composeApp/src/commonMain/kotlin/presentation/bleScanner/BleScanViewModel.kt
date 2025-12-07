package presentation.bleScanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.BleDevice
import domain.repository.BleScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class BleScanUiState(
    val isScanning: Boolean = false,
    val devices: List<ScannedDeviceUi> = emptyList()
)

data class ScannedDeviceUi(
    val device: BleDevice,
    val lastSeen: Long
)

@ExperimentalTime
class BleScanViewModel(
    private val scanner: BleScanner
) : ViewModel() {
    private val currentDevices = mutableMapOf<String, ScannedDeviceUi>()
    private val _uiState = MutableStateFlow<BleScanUiState>(BleScanUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanner.scannedDevices.collect { bleDevice ->
                val now = Clock.System.now().toEpochMilliseconds()

                val uiModel = ScannedDeviceUi(
                    device = bleDevice,
                    lastSeen = now
                )

                currentDevices[bleDevice.platformAddress] = uiModel
                updateDevicesList()
            }

            scanner.isScanning.collect { isScanning ->
                _uiState.update { currentState ->
                    currentState.copy(isScanning = isScanning)
                }
            }
        }

        startPruning()
    }

    fun startScan() {
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    private fun startPruning() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val now = Clock.System.now().toEpochMilliseconds()

                val removed = currentDevices.values.removeAll { uiModel ->
                    (now - uiModel.lastSeen) > 5000
                }

                if (removed) updateDevicesList()
            }
        }
    }

    private fun updateDevicesList() {
        val sortedDevices = currentDevices.values
            .sortedByDescending { it.device.rssi }
            .toList()

        _uiState.update { currentState ->
            currentState.copy(devices = sortedDevices)
        }
    }
}