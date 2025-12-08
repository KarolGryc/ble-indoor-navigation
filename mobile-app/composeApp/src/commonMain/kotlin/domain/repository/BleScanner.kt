package domain.repository

import domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BleScanner {
    val scannedDevices: Flow<BleDevice>
    val errors: SharedFlow<BleScanError>
    val isScanning: StateFlow<Boolean>

    fun startScan()
    fun stopScan()
}