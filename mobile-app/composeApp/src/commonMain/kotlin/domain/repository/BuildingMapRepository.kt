package domain.repository

import domain.model.BuildingMapModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BuildingMapRepository {
    suspend fun getAvailableMapsInfo(): List<MapInfo>

    suspend fun getMap(buildingUuid: Uuid): BuildingMapModel

    suspend fun saveMap(buildingMap: BuildingMapModel)
}