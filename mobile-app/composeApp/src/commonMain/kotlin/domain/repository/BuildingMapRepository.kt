package domain.repository

import domain.model.BuildingMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BuildingMapRepository {
    suspend fun getAvailableMapsInfo(): List<MapInfo>
    suspend fun getMap(buildingUuid: Uuid): BuildingMap
    suspend fun addMap(name: String, buildingMap: BuildingMap)
    suspend fun removeMap(buildingUuid: Uuid)
}