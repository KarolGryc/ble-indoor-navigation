package presentation.navigationScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Building
import domain.repository.BuildingMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MapScreenUiState(
    val map: Building? = null,
    val isLoadingMap: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalUuidApi::class)
class MapNavigationViewModel(
    private val buildingId: Uuid,
    private val mapRepository: BuildingMapRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMap()
    }

    private fun loadMap() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMap = true, error = null)
            try {
                val buildingMap = mapRepository.getBuildingMap(buildingId)
                _uiState.value = _uiState.value.copy(map = buildingMap, isLoadingMap = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMap = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

}