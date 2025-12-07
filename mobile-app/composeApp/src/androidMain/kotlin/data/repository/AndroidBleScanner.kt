package data.repository

import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import domain.model.BleDevice
import domain.repository.BleScanError
import domain.repository.BleScanner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

fun ScanResult.toDomain(context: Context): BleDevice {
    val name = when{
        (context.checkSelfPermission(BLUETOOTH_SCAN) == PERMISSION_GRANTED) ->
            this.scanRecord?.deviceName
        else -> "Unknown"
    }

    val tagId = this.scanRecord
        ?.getManufacturerSpecificData(0xFFFF)
        ?.takeIf { it.size == 4 }
        ?.let { it ->
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

class AndroidBleScanner(
    private val context: Context
) : BleScanner {
    override val scannedDevices = MutableSharedFlow<BleDevice>()
    override val isScanning = MutableStateFlow(false)
    override val errors = MutableSharedFlow<BleScanError>()

    private val _bluetoothManager =  (context.getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)
    private val _bluetoothAdapter = _bluetoothManager?.adapter
    private val _leScanner = _bluetoothAdapter?.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.toDomain(context)
            scannedDevices.tryEmit(device)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning.value = false
            errors.tryEmit(BleScanError.ScanFailed)
        }
    }

    override fun startScanning() {
        if (!isScanning.value) return

        try {
            if (context.checkSelfPermission(BLUETOOTH_SCAN) != PERMISSION_GRANTED) {
                errors.tryEmit(BleScanError.LocationPermissionDenied)
                return
            }

            _leScanner?.startScan(scanCallback)
        } catch(e: Exception) {
            errors.tryEmit(BleScanError.ScanFailed)
        }
    }

    override fun stopSCanning() {
        if (!isScanning.value) return

        try {
            if (context.checkSelfPermission(BLUETOOTH_SCAN) != PERMISSION_GRANTED) {
                errors.tryEmit(BleScanError.LocationPermissionDenied)
                return
            }

            _leScanner?.stopScan(scanCallback)
        } catch(e: Exception) {
            errors.tryEmit(BleScanError.ScanFailed)
        } finally {
            isScanning.value = false
        }
    }
}