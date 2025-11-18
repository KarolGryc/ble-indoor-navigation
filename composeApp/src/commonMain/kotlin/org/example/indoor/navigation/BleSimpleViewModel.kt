package org.example.indoor.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BleSimpleViewModel(
    private val bluetoothScanner: BluetoothScanner
) : ViewModel(), BluetoothScanListener {
    private val _currentBleDevices = MutableStateFlow<List<BleScanResult>>(emptyList())
    val currentBleDevices = _currentBleDevices.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _numOfScans = MutableStateFlow(0)
    val numOfScans = _numOfScans.asStateFlow()

    init {
        viewModelScope.launch {
            while(true) {
                pruneOldDevices(5000L)
                delay(1000)
            }
        }
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

    private fun pruneOldDevices(cutOffTime: Long) {
        val prunedList = _currentBleDevices.value.filter { it.timestamp >= cutOffTime }
        _currentBleDevices.value = prunedList
    }

    fun startScan() {
        bluetoothScanner.startScan(this)
    }

    fun stopScan() {
        bluetoothScanner.stopScan()
    }
}