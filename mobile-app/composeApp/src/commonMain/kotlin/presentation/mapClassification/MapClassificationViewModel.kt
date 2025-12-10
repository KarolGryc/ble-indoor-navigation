package presentation.mapClassification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Building
import domain.model.Fingerprint
import domain.model.Measurement
import domain.model.Rssi
import domain.model.TagId
import domain.model.Zone
import domain.repository.BleScanner
import domain.repository.BuildingMapRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface CalibrationStage {
    data object ZoneSelect : CalibrationStage
    data class SignalsRecording(val zoneUiItem: ZoneUiItem, val progress: Float) : CalibrationStage
    data class Result(val zoneUiItem: ZoneUiItem, val fingerprint: Fingerprint) : CalibrationStage
    sealed interface Error : CalibrationStage {
        data object BuildingLoadFailed: Error
    }
}

data class CalibrationUiState(
    val buildingName: String = "",
    val floors: List<FloorUiItem> = emptyList(),
    val currentStage: CalibrationStage = CalibrationStage.ZoneSelect,
    val unsavedData: Boolean = false
)

data class FloorUiItem(
    val name: String,
    val zones: List<ZoneUiItem>
)

@OptIn(ExperimentalUuidApi::class)
data class ZoneUiItem(
    val id: Uuid,
    val name: String,
    val fingerprintCount: Int = 0,
)

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MapClassificationViewModel(
    private val mapRepository: BuildingMapRepository,
    private val scanner: BleScanner,
    private val buildingId: Uuid,
) : ViewModel() {
    private val _state = MutableStateFlow(CalibrationUiState())
    val state = _state.asStateFlow()

    private var _building: Building? = null
    private var _buildingName: String = ""
    private var _recordingJob: Job? = null

    private val scanDuration = 3000L

    init {
        loadBuildingData()
    }

    private fun loadBuildingData() {
        viewModelScope.launch {
            try {
                _building = mapRepository.getBuilding(buildingId)
                _buildingName = _building?.let { mapRepository.getMapInfo(buildingId)?.name } ?: ""
                updateUiState(CalibrationStage.ZoneSelect)
            } catch (_: Exception) {
                _state.update { it.copy(currentStage = CalibrationStage.Error.BuildingLoadFailed) }
            }
        }
    }

    fun recordData(zoneId: Uuid) {
        if (_state.value.currentStage is CalibrationStage.SignalsRecording) return

        _recordingJob = viewModelScope.launch {
            val selectedZone = getZoneWithId(zoneId) ?: return@launch
            val zoneUiItem = ZoneUiItem(
                id = selectedZone.id,
                name = selectedZone.name,
                fingerprintCount = selectedZone.fingerprints.size
            )

            updateUiState(CalibrationStage.SignalsRecording(zoneUiItem, 0f))

            scanner.startScan()

            try {
                val measurements = mutableListOf<Measurement>()
                val collectorJob = launch {
                    scanner.scannedDevices.collect { device ->
                        if (device.tagId != null) {
                            val newMeasurement = Measurement(device.tagId, device.rssi)
                            measurements.add(newMeasurement)
                        }
                    }
                }

                val startTime = now().toEpochMilliseconds()
                val endTime = startTime + scanDuration
                while (now().toEpochMilliseconds() < endTime) {
                    val now = now().toEpochMilliseconds()
                    updateUiState(
                        calibrationStage = CalibrationStage.SignalsRecording(
                            zoneUiItem = zoneUiItem,
                            progress = calculateProgress(startTime, now)
                        )
                    )
                    delay(100.milliseconds)
                }
                collectorJob.cancel()

                updateUiState(
                    calibrationStage = CalibrationStage.Result(
                        zoneUiItem = zoneUiItem,
                        fingerprint = measurementsToFingerprint(measurements)
                    )
                )
            } finally {
                scanner.stopScan()
            }
        }
    }

    fun acceptPendingFingerprint() {
        val currentStage = _state.value.currentStage
        if (currentStage is CalibrationStage.Result) {
            val zone = getZoneWithId(currentStage.zoneUiItem.id)
            zone?.fingerprints?.add(currentStage.fingerprint)
            resetCalibrationStage()
            _state.update { it.copy(unsavedData = true) }
        }
    }

    fun resetCalibrationStage() {
        updateUiState(calibrationStage = CalibrationStage.ZoneSelect)
    }

    fun persistBuildingConfig() {
        _building?.let { building ->
            viewModelScope.launch {
                mapRepository.addMap(name = _buildingName, building = building)
                _state.update { it.copy(unsavedData = false) }
            }
        }
    }

    fun resetCalibration() {
        _recordingJob?.cancel()
        _building?.floors?.forEach { floor ->
            floor.zones.forEach { zone ->
                zone.fingerprints.clear()
            }
        }
        _state.update { it.copy(unsavedData = true) }

        updateUiState(CalibrationStage.ZoneSelect)
    }

    private fun getZoneWithId(zoneId: Uuid): Zone? {
        for (floor in _building?.floors ?: emptyList()) {
            for (zone in floor.zones) {
                if (zone.id == zoneId) {
                    return zone
                }
            }
        }

        return null
    }

    private fun measurementsToFingerprint(measurements: List<Measurement>): Fingerprint {
        val tagRssiMap = mutableMapOf<TagId, MutableList<Rssi>>()

        measurements.forEach { measurement ->
            tagRssiMap
                .getOrPut(measurement.tagId, { mutableListOf() })
                .add(measurement.rssi)
        }

        return Fingerprint(
            measurements = tagRssiMap.map {
                Measurement (
                    tagId = it.key,
                    rssi = it.value.average().toInt()
                )
            }
        )
    }

    private fun getUiFloors(building: Building?): List<FloorUiItem> {
        return building?.floors?.map { floor ->
            val floorZoneUiItems = floor.zones.map {
                ZoneUiItem(it.id, it.name, it.fingerprints.size)
            }

            FloorUiItem (
                name = floor.name,
                zones = floorZoneUiItems
            )
        } ?: emptyList()
    }

    private fun calculateProgress(startTime: Long, now: Long): Float {
        return ((now - startTime).toFloat() / scanDuration.toFloat()).coerceIn(0f, 1f)
    }

    private fun updateUiState(calibrationStage: CalibrationStage) {
        _state.update { it.copy(
            buildingName = _buildingName,
            floors = getUiFloors(_building),
            currentStage = calibrationStage
        ) }
    }
}