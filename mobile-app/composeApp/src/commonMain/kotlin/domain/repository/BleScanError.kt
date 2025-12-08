package domain.repository

sealed interface BleScanError{
    object None : BleScanError
    object BluetoothDisabled : BleScanError
    object LocationPermissionDenied : BleScanError
    object ScanFailed : BleScanError
}