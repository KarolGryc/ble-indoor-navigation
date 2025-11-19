package org.example.indoor.navigation

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class BluetoothScanner {
    fun startScan(listener: BluetoothScanListener): ScanStartResult
    fun stopScan()
}