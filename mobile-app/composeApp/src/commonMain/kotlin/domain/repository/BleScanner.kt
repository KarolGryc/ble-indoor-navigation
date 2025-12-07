package domain.repository

import domain.model.BleDevice
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface BleScanError{
    object BluetoothDisabled : BleScanError
    object LocationPermissionDenied : BleScanError
    object ScanFailed : BleScanError
}

interface BleScanner {
    val scannedDevices: SharedFlow<BleDevice>
    val errors: SharedFlow<BleScanError>
    val isScanning: StateFlow<Boolean>

    fun startScan()
    fun stopScan()
}