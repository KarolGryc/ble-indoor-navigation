package presentation.bleScanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.BleDevice
import domain.repository.BleScanError
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
    val error: BleScanError? = null,
    val devices: List<ScannedDeviceUi> = emptyList()
)

data class ScannedDeviceUi(
    val device: BleDevice,
    val seenAgo: Long
)

private data class DeviceEntry(
    val device: BleDevice,
    val timestamp: Long
)

@ExperimentalTime
class BleScanViewModel(
    private val scanner: BleScanner
) : ViewModel() {
    private val currentDevices = mutableMapOf<String, DeviceEntry>()

    private val _uiState = MutableStateFlow(BleScanUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanner.scannedDevices.collect { bleDevice ->
                val now = Clock.System.now().toEpochMilliseconds()
                currentDevices[bleDevice.platformAddress] = DeviceEntry(
                    device = bleDevice,
                    timestamp = now
                )
            }
        }

        viewModelScope.launch {
            scanner.isScanning.collect { isScanning ->
                _uiState.update { it.copy(isScanning = isScanning) }
            }
        }

        viewModelScope.launch {
            scanner.errors.collect { error ->
                _uiState.update { it.copy(error = error) }
            }
        }

        startUiLoop()
    }

    fun startScan() {
        scanner.startScan()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    private fun startUiLoop() {
        viewModelScope.launch {
            while (true) {
                delay(250)

                val now = Clock.System.now().toEpochMilliseconds()
                val timeout = 5000L

                val iterator = currentDevices.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if ((now - entry.value.timestamp) > timeout) {
                        iterator.remove()
                    }
                }

                updateDevicesList(now)
            }
        }
    }

    private fun updateDevicesList(now: Long) {
        val sortedDevices = currentDevices.values
            .map { entry ->
                ScannedDeviceUi(
                    device = entry.device,
                    seenAgo = now - entry.timestamp
                )
            }
            .sortedByDescending { it.device.rssi }
            .toList()

        _uiState.update { currentState ->
            currentState.copy(devices = sortedDevices)
        }
    }
}