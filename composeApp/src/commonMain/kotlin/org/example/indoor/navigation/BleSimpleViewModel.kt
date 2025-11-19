package org.example.indoor.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class BleSimpleViewModel(
    private val bluetoothScanner: BluetoothScanner
) : ViewModel(), BluetoothScanListener {
    private val _cutOffTime = 5000L

    private val _currentBleDevices = MutableStateFlow<List<BleScanResult>>(emptyList())
    val currentBleDevices = _currentBleDevices.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _numOfScans = MutableStateFlow(0)
    val numOfScans = _numOfScans.asStateFlow()

    init {
        viewModelScope.launch {
            while(true) {
                pruneOldDevices(_cutOffTime)
                delay(1000)
            }
        }
    }

    fun startScan() {
        bluetoothScanner.startScan(this)
    }

    fun stopScan() {
        bluetoothScanner.stopScan()
    }

    override fun onNewScan(result: ScanCallbackResult) {
        when (result) {
            is ScanCallbackResult.Success -> handleIncomingScan(result.scannedDevice)
            is ScanCallbackResult.PermissionMissing -> {
                _errorState.value = "Missing permissions: ${result.permission.joinToString()}"
                stopScan()
            }
        }
    }

    private fun handleIncomingScan(result: BleScanResult) {
        val currentList = _currentBleDevices.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.identifier == result.identifier }
        if (existingIndex != -1) {
            currentList[existingIndex] = result
        } else {
            currentList.add(result)
        }
        _currentBleDevices.value = currentList
        _numOfScans.value += 1
    }

    @OptIn(ExperimentalTime::class)
    private fun pruneOldDevices(cutOffTimeMs: Long) {
        val prunedList = _currentBleDevices.value.filter {
            !hasPassedMillis(it.timestamp, cutOffTimeMs)
        }
        _currentBleDevices.value = prunedList
    }
}