package presentation.buildingNavigation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Building
import domain.model.Floor
import domain.model.Zone
import domain.repository.BleScanError
import domain.repository.BleScanner
import domain.repository.BuildingMapRepository
import domain.service.CompassService
import domain.service.LocationService
import domain.usecase.RecordFingerprintUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import utils.Filter
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MapNavigationViewModel(
    private val buildingId: Uuid,
    private val mapRepository: BuildingMapRepository,
    private val compassSensor: CompassService,
    private val locationService: LocationService,
    private val recordFingerprintUseCase: RecordFingerprintUseCase,
    private val scanner: BleScanner
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _viewportState = MutableStateFlow(ViewportState())
    val viewportState = _viewportState.asStateFlow()

    private val _isCompassModeEnabled = MutableStateFlow(false)
    val isCompassModeEnabled = _isCompassModeEnabled.asStateFlow()

    private var _compassJob: Job? = null
    private var _locationJob: Job? = null


    init {
        loadMap()

        viewModelScope.launch {
            scanner.isScanning.collect { isScanning ->
                _uiState.update { it.copy(locationEnabled = isScanning) }
            }
        }

        viewModelScope.launch {
            scanner.errors.collect { error ->
                when (error) {
                    is BleScanError.BluetoothDisabled -> {
                        setErrorMessage(
                            title = "Bluetooth Disabled",
                            message = "Please enable Bluetooth to use location tracking."
                        )
                        _uiState.update { it.copy(locationEnabled = false) }
                    }
                    is BleScanError.LocationPermissionDenied -> {
                        setErrorMessage(
                            title = "Permission Denied",
                            message = "Required permissions were denied. Please grant them to use location tracking."
                        )
                        _uiState.update { it.copy(locationEnabled = false) }
                    }
                    else -> { /* No action */ }
                }
            }
        }
    }

    fun startLocationTracking() {
        _locationJob?.cancel()
        _uiState.update { it.copy(locationEnabled = true) }

        _locationJob = viewModelScope.launch {
            val filter = Filter<Zone>(2)

            try {
                scanner.startScan()

                var initialMove = false
                while (isActive) {
                    val currentMap = _uiState.value.map

                    if (currentMap == null) {
                        delay(500)
                        continue
                    }

                    val measuredSignals = recordFingerprintUseCase(1000)

                    val estimatedZone = locationService.determineLocation(measuredSignals, currentMap)
                    val stableZone = filter.getNewValue(estimatedZone)

                    if (stableZone != null && stableZone != _uiState.value.currentZone) {
                        _uiState.update { it.copy(currentZoneUuid = stableZone.id) }

                        if (!initialMove) {
                            moveCameraToCurrentZone()
                            initialMove = true
                        }
                    }
                }
            } finally {
                scanner.stopScan()
            }
        }
    }

    fun stopLocationTracking() {
        _locationJob?.cancel()
        _locationJob = null
        scanner.stopScan()
        _uiState.update {
            it.copy(
                locationEnabled = false,
                currentZoneUuid = null,
                currentFloorUuid = null
            )
        }
    }

    fun moveCameraToCurrentZone() {
        _uiState.value.currentZone?.let { moveCameraToZone(it) }
    }

    fun moveCameraToZone(zone: Zone) {
        val (posX, posY) = zone.centerPos
        val scale = _viewportState.value.scale
        val scaledX = -posX * scale // TODO: fix the inverted axis
        val scaledY = -posY * scale
        _uiState.update { it.copy(currentFloorUuid = zone.floor?.id) }
        _viewportState.update { it.copy(offset = Offset(scaledX, scaledY)) }
    }

    fun startCompassMode() {
        _compassJob?.cancel()
        _isCompassModeEnabled.value = true

        _compassJob = viewModelScope.launch {
            compassSensor.azimuth.collect { azimuth ->
                val currentMapRotation = _viewportState.value.rotation

                val targetMapRotation = -azimuth

                val smoothedRotation = smoothAngle(currentMapRotation, targetMapRotation, smoothing = 0.1f)

                _viewportState.update { it.copy(rotation = smoothedRotation) }
            }
        }
    }

    fun stopCompassMode() {
        _compassJob?.cancel()
        _compassJob = null
        _isCompassModeEnabled.value = false
    }

    fun setErrorMessage(title: String, message: String) {
        _uiState.value = _uiState.value.copy(
            uiErrorMessage = ErrorMessage(title, message)
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(uiErrorMessage = null)
    }

    fun incrementFloor() {
        val currentState = _uiState.value
        val floors = currentState.map?.floors ?: return
        val currentIndex = floors.indexOfFirst { it.id == currentState.currentFloorUuid }

        val newIdx = currentIndex + 1
        if (newIdx >= floors.size) return
        _uiState.update { it.copy(currentFloorUuid = floors[newIdx].id) }
    }

    fun decrementFloor() {
        val currentState = _uiState.value
        val floors = currentState.map?.floors ?: return
        val currentIndex = floors.indexOfFirst { it.id == currentState.currentFloorUuid }

        val newIdx = currentIndex - 1
        if (newIdx < 0) return
        _uiState.update { it.copy(currentFloorUuid = floors[newIdx].id) }
    }

    fun updateViewport(
        offset: Offset? = null,
        scale: Float? = null,
        rotation: Float? = null,
        tilt: Float? = null
    ) {
        val clampedScale = scale?.let{ ViewportState().clampZoom(it) }
        _viewportState.value = ViewportState(
            offset = offset ?: _viewportState.value.offset,
            scale = clampedScale ?: _viewportState.value.scale,
            rotation = rotation ?: _viewportState.value.rotation,
            tilt = tilt ?: _viewportState.value.tilt
        )
    }

    fun resetCamera() {
        _viewportState.value = ViewportState()
    }

    fun resetZoom() {
        _viewportState.value = _viewportState.value.copy(scale = ViewportState.DEFAULT_ZOOM)
    }

    fun resetRotation() {
        _viewportState.value = _viewportState.value.copy(rotation = 0f)
    }

    override fun onCleared() {
        super.onCleared()
        stopCompassMode()
        stopLocationTracking()
    }

    private fun loadMap() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMap = true, error = null)
            try {
                val buildingMap = mapRepository.getBuilding(buildingId)
                val firstFloorId = buildingMap.floors.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    map = buildingMap,
                    isLoadingMap = false,
                    currentFloorUuid = firstFloorId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMap = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun smoothAngle(current: Float, target: Float, smoothing: Float = 0.05f): Float {
        val diff = (target - current + 540) % 360 - 180
        val appliedDiff = diff * smoothing

        return (current + appliedDiff) % 360
    }
}

data class ErrorMessage(val title: String, val message: String)

@OptIn(ExperimentalUuidApi::class)
data class MapScreenUiState(
    val map: Building? = null,
    val isLoadingMap: Boolean = false,
    val error: String? = null,

    val locationEnabled: Boolean = false,
    val currentFloorUuid: Uuid? = null,
    val currentZoneUuid: Uuid? = null,

    val uiErrorMessage: ErrorMessage? = null
) {
    val isTopLevel: Boolean
        get() {
            val floors = map?.floors ?: return false
            val currentIndex = floors.indexOfFirst { it.id == currentFloorUuid }
            return currentIndex == floors.size - 1
        }

    val isBottomLevel: Boolean
        get() {
            val floors = map?.floors ?: return false
            val currentIndex = floors.indexOfFirst { it.id == currentFloorUuid }
            return currentIndex == 0
        }

    val floorNum: Int
        get() {
            val floors = map?.floors ?: return 0
            val currentIndex = floors.indexOfFirst { it.id == currentFloorUuid }
            return if (currentIndex != -1) currentIndex else 0
        }

    val currentFloor: Floor?
        get() = map?.floors?.find { it.id == currentFloorUuid }

    val currentZone: Zone?
        get() = map?.floors?.firstNotNullOfOrNull { floor ->
                floor.zones.find { zone -> zone.id == currentZoneUuid }
            }
}

data class ViewportState(
    val offset: Offset = Offset.Zero,
    val scale: Float = 1.5f,
    val rotation: Float = 0.0f,
    val tilt: Float = 1.0f
) {
    companion object {
        const val MAX_ZOOM = 3f
        const val MIN_ZOOM = 0.4f
        const val DEFAULT_ZOOM = 1.5f
    }

    fun clampZoom(zoom: Float): Float {
        return zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    }
}