package domain.repository

import domain.model.Building
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BuildingMapRepository {
    suspend fun getMapInfo(): List<MapInfo>
    suspend fun getMapInfo(uuid: Uuid): MapInfo?
    suspend fun getBuilding(buildingUuid: Uuid): Building
    suspend fun addMap(name: String, building: Building)
    suspend fun removeMap(buildingUuid: Uuid)
    suspend fun renameMap(buildingUuid: Uuid, newDisplayName: String)
}