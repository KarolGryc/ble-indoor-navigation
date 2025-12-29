package domain.usecase

import domain.extensions.averageRssiByTag
import domain.model.Fingerprint
import domain.model.Measurement
import domain.repository.BleScanner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecordFingerprintUseCase(
    private val scanner: BleScanner
) {
    suspend operator fun invoke(collectionTime: Long) : Fingerprint = coroutineScope {
        val measurements = mutableListOf<Measurement>()

        val collectionJob = launch {
            scanner.scannedDevices.collect { device ->
                if (device.tagId != null) {
                    measurements.add(Measurement(device.tagId, device.rssi))
                }
            }
        }

        delay(collectionTime)
        collectionJob.cancel()

        Fingerprint(measurements.averageRssiByTag())
    }
}