package presentation.navigationScreen

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Building
import domain.model.Floor
import domain.repository.BuildingMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class MapScreenUiState(
    val map: Building? = null,
    val selectedFloorUuid: Uuid? = null,
    val isLoadingMap: Boolean = false,
    val error: String? = null
) {
    val selectedFloor: Floor?
        get() = map?.floors?.find { it.id == selectedFloorUuid }

    val isTopLevel: Boolean
        get() {
            val floors = map?.floors ?: return false
            val currentIndex = floors.indexOfFirst { it.id == selectedFloorUuid }
            return currentIndex == floors.size -1
        }

    val isBottomLevel: Boolean
        get() {
            val floors = map?.floors ?: return false
            val currentIndex = floors.indexOfFirst { it.id == selectedFloorUuid }
            return currentIndex == 0
        }
}

data class ViewportState(
    val offset: Offset = Offset.Zero,
    val scale: Float = 1.5f,
    val rotation: Float = 0.0f,
    val tilt: Float = 0.75f
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

@OptIn(ExperimentalUuidApi::class)
class MapNavigationViewModel(
    private val buildingId: Uuid,
    private val mapRepository: BuildingMapRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _viewportState = MutableStateFlow(ViewportState())
    val viewportState = _viewportState.asStateFlow()

    init {
        loadMap()
    }

    // --- Floor Selection ---
    fun selectFloor(floorId: Uuid) {
        _uiState.value = _uiState.value.copy(selectedFloorUuid = floorId)
    }

    fun changeFloorUp() {
        _uiState.value = _uiState.value.let { currentState ->
            val floors = currentState.map?.floors ?: return
            val currentIndex = floors.indexOfFirst { it.id == currentState.selectedFloorUuid }

            val newIdx = currentIndex + 1
            if (newIdx >= floors.size) return
            currentState.copy(selectedFloorUuid = floors[newIdx].id)
        }
    }

    fun changeFloorDown() {
        _uiState.value = _uiState.value.let { currentState ->
            val floors = currentState.map?.floors ?: return
            val currentIndex = floors.indexOfFirst { it.id == currentState.selectedFloorUuid }

            val newIdx = currentIndex - 1
            if (newIdx < 0) return
            currentState.copy(selectedFloorUuid = floors[newIdx].id)
        }
    }

    // --- Viewport ---
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

    private fun loadMap() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMap = true, error = null)
            try {
                val buildingMap = mapRepository.getBuildingMap(buildingId)
                val firstFloorId = buildingMap.floors.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    map = buildingMap,
                    isLoadingMap = false,
                    selectedFloorUuid = firstFloorId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMap = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}