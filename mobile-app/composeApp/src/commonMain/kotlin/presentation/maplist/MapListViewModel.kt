package presentation.maplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.BuildingMapDto
import data.mapper.BuildingMapper
import domain.model.Building
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MapListUiState(
    val isLoading: Boolean = false,
    val mapData: List<MapInfo> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(ExperimentalUuidApi::class)
class MapListViewModel(
    private val mapRepository: BuildingMapRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapListUiState())
    val  uiState = _uiState.asStateFlow()

    init {
        loadMapList()
    }

    fun loadMapList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val maps = mapRepository.getMapsInfo()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mapData = maps,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun addMap(name: String, building: Building) {
        viewModelScope.launch {
            mapRepository.addMap(name, building)
            loadMapList()
        }
    }

    fun addMap(name: String, buildingMap: ByteArray) {
        val jsonString = buildingMap.decodeToString()
        val jsonParser = Json { ignoreUnknownKeys = true }
        val dto = jsonParser.decodeFromString<BuildingMapDto>(jsonString)
        val domainMap = BuildingMapper.mapToDomain(dto)

        addMap(name, domainMap)
    }

    fun removeMap(buildingUuid: Uuid) {
        viewModelScope.launch {
            mapRepository.removeMap(buildingUuid)
            loadMapList()
        }
    }

    fun renameMap(buildingUuid: Uuid, newName: String) {
        viewModelScope.launch {
            mapRepository.renameMap(buildingUuid, newName)
            loadMapList()
        }
    }
}