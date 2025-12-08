package presentation.mapClassification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Building
import domain.model.Rssi
import domain.model.TagId
import domain.model.ZoneFingerprint
import domain.repository.BleScanner
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class CalibrationUiState(
    val buildingName: String = "",
    val floors: List<FloorUiItem> = emptyList(),

    val isRecording: Boolean = false,
    val activeZoneId: Uuid? = null,
    val recordingProgress: Float = 0f,

    val error: String? = null
)

data class FloorUiItem(
    val floorName: String,
    val zones: List<ZoneUiItem>
)

@OptIn(ExperimentalUuidApi::class)
data class ZoneUiItem(
    val id: Uuid,
    val name: String,
    val recordDataCount: Int = 0,
)
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MapClassificationViewModel(
    private val buildingId: Uuid,
    private val mapRepository: BuildingMapRepository,
    private val scanner: BleScanner
) : ViewModel() {
    private val _state = MutableStateFlow(CalibrationUiState())
    val state = _state.asStateFlow()

    private var currentBuilding: Building? = null
    private var recordingJob: Job? = null

    init {
        loadBuildingData()
    }

    private fun loadBuildingData() {
        viewModelScope.launch {
            try {
                val building = mapRepository.getBuildingMap(buildingId)
                currentBuilding = building
                updateUiFromBuilding(building)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error while loading map: ${e.message}") }
            }
        }
    }

    fun onRecordDataClick(zoneId: Uuid) {
        if (_state.value.isRecording) return

        recordingJob = viewModelScope.launch {
            _state.update { it.copy(
                isRecording = true,
                activeZoneId = zoneId,
                recordingProgress = 0f
            )}

            scanner.startScan()

            val collectedRssi = mutableMapOf<TagId, MutableList<Rssi>>()
            val scanDuration = 3000L
            val startTime = Clock.System.now().toEpochMilliseconds()

            try {
                val endTime = startTime + scanDuration

                val collectorJob = launch {
                    scanner.scannedDevices.collect { device ->
                        if (device.tagId != null) {
                            collectedRssi
                                .getOrPut(device.tagId) { mutableListOf() }
                                .add(device.rssi)
                        }
                    }
                }

                while (Clock.System.now().toEpochMilliseconds() < endTime) {
                    val now = Clock.System.now().toEpochMilliseconds()
                    val progress = (now - startTime).toFloat() / scanDuration.toFloat()
                    _state.update { it.copy(recordingProgress = progress.coerceIn(0f, 1f)) }
                    delay(100.milliseconds)
                }

                collectorJob.cancel()

            } finally {
                scanner.stopScan()
            }

            saveFingerprint(zoneId, collectedRssi)
        }
    }

    private fun saveFingerprint(zoneId: Uuid, rawData: Map<TagId, List<Rssi>>) {
        val building = currentBuilding ?: return

        val averagedReadings = rawData.mapValues { (_, rssiList) ->
            rssiList.average().toInt()
        }

        val newFingerprint = ZoneFingerprint(
            zoneId = zoneId,
            fingerprints = averagedReadings
        )

        val updatedRadioMap = building.fingerprintsMap + newFingerprint

        val updatedBuilding = building.copy(fingerprintsMap = updatedRadioMap)
        currentBuilding = updatedBuilding

        viewModelScope.launch {
            try {
                val mapInfos = mapRepository.getMapsInfo(uuid = building.id)
                mapRepository.addMap(mapInfos?.name ?: "Building", updatedBuilding)

                updateUiFromBuilding(updatedBuilding)
                _state.update { it.copy(isRecording = false, activeZoneId = null, recordingProgress = 0f) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error while saving: ${e.message}", isRecording = false) }
            }
        }
    }

    private suspend fun updateUiFromBuilding(building: Building) {
        val floorsUi = building.floors.map { floor ->
            FloorUiItem(
                floorName = floor.name,
                zones = floor.zones.map { zone ->
                    ZoneUiItem(
                        id = zone.id,
                        name = zone.name,
                        recordDataCount = building.fingerprintsMap.count { it.zoneId == zone.id }
                    )
                }
            )
        }

        val (name, _) = mapRepository.getMapsInfo(uuid = building.id) ?: MapInfo("Unnamed", building.id)
        _state.update { it.copy(buildingName = name, floors = floorsUi) }
    }
}