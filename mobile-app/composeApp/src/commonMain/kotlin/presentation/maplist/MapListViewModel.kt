package presentation.maplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.dto.BuildingMapDto
import data.mapper.BuildingMapper
import domain.model.BuildingMap
import domain.repository.BuildingMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class MapListUiState(
    val isLoading: Boolean = false,
    val mapNames: List<String> = emptyList(),
    val errorMessage: String? = null
)

class MapListViewModel(
    private val mapRepository: BuildingMapRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapListUiState())
    val  uiState = _uiState.asStateFlow()

    fun loadMapList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val maps = mapRepository.getAvailableMapsInfo()
                val mapNames = maps.map { it.name }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mapNames = mapNames,
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

    fun addMap(name: String, buildingMap: BuildingMap) {
        viewModelScope.launch {
            mapRepository.addMap(name, buildingMap)
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
}