package org.example.indoor.navigation

sealed class ScanCallbackResult {
    data class Success(val scannedDevice: BleScanResult): ScanCallbackResult()
    data class PermissionMissing(val permission: List<String>): ScanCallbackResult()
}