package org.example.indoor.navigation

sealed class ScanStartResult {
    object Started: ScanStartResult()
    object BluetoothDisabled: ScanStartResult()
    object ScannerUnavailable: ScanStartResult()
    data class PermissionMissing(val permission: List<String>): ScanStartResult()
}

sealed class ScanCallbackResult {
    data class Success(val scannedDevice: BleScanResult): ScanCallbackResult()
    data class PermissionMissing(val permission: List<String>): ScanCallbackResult()
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class BluetoothScanner {
    fun startScan(listener: BluetoothScanListener): ScanStartResult
    fun stopScan()
}