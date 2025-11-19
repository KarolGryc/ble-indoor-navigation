package org.example.indoor.navigation

sealed class ScanStartResult {
    object Started: ScanStartResult()
    object BluetoothDisabled: ScanStartResult()
    object ScannerUnavailable: ScanStartResult()
    data class PermissionMissing(val permission: List<String>): ScanStartResult()
}