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
import presentation.buildingNavigation.ErrorMessage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ExperimentalTime
class BleScanViewModel(
    private val scanner: BleScanner
) : ViewModel() {
    private val currentDevices = mutableMapOf<String, DeviceEntry>()

    private val _uiState = MutableStateFlow(BleScanUiState())
    val uiState = _uiState.asStateFlow()

    var filterByName: Boolean = false
    val tagName = "InNav Tag"

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

    fun toggleScanButton() {
        if (_uiState.value.isScanning) {
            scanner.stopScan()
        } else {
            scanner.startScan()
        }
    }

    fun setErrorMessage(message: ErrorMessage?) {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = message)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
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
            .filter {
                if (filterByName) it.device.name?.contains(tagName, ignoreCase = true) == true
                else true
            }
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

data class BleScanUiState(
    val isScanning: Boolean = false,
    val error: BleScanError = BleScanError.None,
    val devices: List<ScannedDeviceUi> = emptyList(),

    val errorMessage: ErrorMessage? = null
)

data class ScannedDeviceUi(
    val device: BleDevice,
    val seenAgo: Long
)

private data class DeviceEntry(
    val device: BleDevice,
    val timestamp: Long
)