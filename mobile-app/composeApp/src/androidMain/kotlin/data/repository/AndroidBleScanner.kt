package data.repository

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.util.Log
import domain.model.BleDevice
import domain.repository.BleScanError
import domain.repository.BleScanner
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

fun ScanResult.toDomain(context: Context): BleDevice {
    val name: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        when {
            (context.checkSelfPermission(BLUETOOTH_SCAN) == PERMISSION_GRANTED) ->
                this.scanRecord?.deviceName ?: "Unknown"
            else -> "Unknown"
        }
    } else {
        this.scanRecord?.deviceName ?: "Unknown"
    }

    val tagId = this.scanRecord
        ?.getManufacturerSpecificData(0xFFFF)
        ?.takeIf { it.size == 4 }
        ?.let {
            ((it[0].toInt() and 0xFF) or
            ((it[1].toInt() and 0xFF) shl 8) or
            ((it[2].toInt() and 0xFF) shl 16) or
            ((it[3].toInt() and 0xFF) shl 24))
        }

    return BleDevice(
        name = name,
        platformAddress = this.device.address,
        rssi = this.rssi,
        tagId = tagId
    )
}
//
//class AndroidBleScanner(
//    private val context: Context
//) : BleScanner {
//    private val _scannedDevices = MutableSharedFlow<BleDevice>(
//        replay = 0,
//        extraBufferCapacity = 100,
//        onBufferOverflow = BufferOverflow.DROP_OLDEST
//    )
//    override val scannedDevices: Flow<BleDevice> = _scannedDevices
//    override val isScanning = MutableStateFlow(false)
//    override val errors = MutableSharedFlow<BleScanError>(
//        replay = 0,
//        extraBufferCapacity = 100,
//        onBufferOverflow = BufferOverflow.DROP_OLDEST
//    )
//
//    private val _bluetoothManager =  (context.getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)
//    private val _bluetoothAdapter = _bluetoothManager?.adapter
//    private val _leScanner = _bluetoothAdapter?.bluetoothLeScanner
//
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            val device = result.toDomain(context)
//            _scannedDevices.tryEmit(device)
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            isScanning.value = false
//            errors.tryEmit(BleScanError.ScanFailed)
//        }
//    }
//
//    override fun startScan() {
//        if (_bluetoothAdapter?.isEnabled != true) {
//            errors.tryEmit(BleScanError.BluetoothDisabled)
//            return
//        }
//
//        if (isScanning.value) return
//
//        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            context.checkSelfPermission(BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
//        } else {
//            context.checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        }
//
//        if (!hasPermission) {
//            errors.tryEmit(BleScanError.LocationPermissionDenied)
//            return
//        }
//
//        try {
//            _leScanner?.startScan(scanCallback)
//
//            isScanning.value = true
//            Log.d("AndroidBleScanner", "BLE scan started")
//        } catch (e: Exception) {
//            Log.e("AndroidBleScanner", "Start scan failed", e)
//            errors.tryEmit(BleScanError.ScanFailed)
//            isScanning.value = false
//        }
//    }
//
//    override fun stopScan() {
//        if (!isScanning.value) return
//
//        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            context.checkSelfPermission( BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
//        } else {
//            context.checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        }
//
//        if (!hasPermission) {
//            isScanning.value = false
//            return
//        }
//
//        try {
//            _leScanner?.stopScan(scanCallback)
//        } catch (e: Exception) {
//            errors.tryEmit(BleScanError.ScanFailed)
//        } finally {
//            isScanning.value = false
//        }
//    }
//}

class AndroidBleScanner(
    private val context: Context
) : BleScanner {
    private val _scannedDevices = MutableSharedFlow<BleDevice>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val scannedDevices: Flow<BleDevice> = _scannedDevices
    override val isScanning = MutableStateFlow(false)
    override val errors = MutableSharedFlow<BleScanError>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _bluetoothManager = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
    private val _bluetoothAdapter = _bluetoothManager?.adapter

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                when (state) {
                    BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                        if (isScanning.value) {
                            Log.d("AndroidBleScanner", "Bluetooth turned off externally. Stopping scan.")
                            isScanning.value = false
                            errors.tryEmit(BleScanError.BluetoothDisabled)
                        }
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.toDomain(context)
            _scannedDevices.tryEmit(device)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning.value = false
            errors.tryEmit(BleScanError.ScanFailed)
        }
    }

    override fun startScan() {
        if (_bluetoothAdapter == null || !_bluetoothAdapter.isEnabled) {
            errors.tryEmit(BleScanError.BluetoothDisabled)
            return
        }

        if (isScanning.value) return

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(BLUETOOTH_SCAN) == PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        }

        if (!hasPermission) {
            errors.tryEmit(BleScanError.LocationPermissionDenied)
            return
        }

        try {
            val scanner = _bluetoothAdapter.bluetoothLeScanner ?: _bluetoothAdapter.let {
                errors.tryEmit(BleScanError.BluetoothDisabled)
                return
            }

            scanner.startScan(scanCallback)
            isScanning.value = true
        } catch (_: Exception) {
            errors.tryEmit(BleScanError.ScanFailed)
            isScanning.value = false
        }
    }

    override fun stopScan() {
        if (!isScanning.value) return

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission( BLUETOOTH_SCAN) == PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        }

        if (!hasPermission) {
            isScanning.value = false
            return
        }

        try {
            val scanner = _bluetoothAdapter?.bluetoothLeScanner ?: _bluetoothAdapter.let {
                errors.tryEmit(BleScanError.BluetoothDisabled)
                return
            }

            scanner.stopScan(scanCallback)
        } catch (_: Exception) {
            errors.tryEmit(BleScanError.ScanFailed)
        } finally {
            isScanning.value = false
        }
    }
}