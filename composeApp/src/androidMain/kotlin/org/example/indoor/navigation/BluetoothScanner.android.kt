package org.example.indoor.navigation

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.core.util.forEach
import kotlin.time.ExperimentalTime

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class BluetoothScanner (
    val context: Context
) {
    private val _bluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private val _leScanner = _bluetoothAdapter?.bluetoothLeScanner
    private var _listener: BluetoothScanListener? = null

    @OptIn(ExperimentalTime::class)
    private var _scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!canReadBleDeviceName(context)) {
                val error = ScanCallbackResult.PermissionMissing(missingPermissions)
                _listener?.onNewScan(error)
            }

            val device = result.device
            val name = device.name
            val macAddress = device.address
            val rssi = result.rssi
            val timestamp = nowMillis()
            val manufacturerData = buildList {
                result.scanRecord?.manufacturerSpecificData?.forEach { companyId, data ->
                    add(Pair(companyId, data))
                }
            }

            val scannedDevice = BleScanResult(
                name = name,
                identifier = macAddress,
                manufacturerData = manufacturerData,
                rssi = rssi,
                timestamp = timestamp
            )
            _listener?.onNewScan(ScanCallbackResult.Success(scannedDevice))
        }
    }


    @SuppressLint("MissingPermission")
    actual fun startScan(listener: BluetoothScanListener): ScanStartResult {
        when {
            missingPermissions.isNotEmpty() ->
                return ScanStartResult.PermissionMissing(missingPermissions)
            !isScannerAvailable() ->
                return ScanStartResult.ScannerUnavailable
            !isBluetoothEnabled() ->
                return ScanStartResult.BluetoothDisabled
        }

        _listener = listener
        _leScanner?.startScan(_scanCallback)
        return ScanStartResult.Started
    }

    actual fun stopScan() {
        if(_listener == null) return
        _listener = null
        _leScanner?.stopScan(_scanCallback)
    }

    private fun isBluetoothEnabled() = _bluetoothAdapter?.isEnabled == true

    private fun isScannerAvailable() = _leScanner != null

    private fun canReadBleDeviceName(context: Context): Boolean {
        val requiresBleConnect = isAtLeastAndroidVersion(android.os.Build.VERSION_CODES.S)
        return !requiresBleConnect || hasPermission(context, BLUETOOTH_CONNECT)
    }

    private val missingPermissions: List<String>
        get() = buildList {
            if (isAtLeastAndroidVersion(android.os.Build.VERSION_CODES.S)) {
                if (!hasPermission(context, BLUETOOTH_SCAN))    add(BLUETOOTH_SCAN)
                if (!hasPermission(context, BLUETOOTH_CONNECT)) add(BLUETOOTH_CONNECT)
            } else {
                if (!hasPermission(context, BLUETOOTH))         add(BLUETOOTH)
                if (!hasPermission(context, BLUETOOTH_ADMIN))   add(BLUETOOTH_ADMIN)
            }
            if (!hasPermission(context ,ACCESS_FINE_LOCATION))  add(ACCESS_FINE_LOCATION)
        }
}