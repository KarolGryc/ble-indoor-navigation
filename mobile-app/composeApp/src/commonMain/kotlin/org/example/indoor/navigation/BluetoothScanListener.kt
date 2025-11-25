package org.example.indoor.navigation

interface BluetoothScanListener {
    fun onNewScan(result: ScanCallbackResult)
}
