package domain.repository

import domain.model.Building
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BuildingMapRepository {
    suspend fun getMapsInfo(): List<MapInfo>
    suspend fun getBuildingMap(buildingUuid: Uuid): Building
    suspend fun addMap(name: String, building: Building)
    suspend fun removeMap(buildingUuid: Uuid)
    suspend fun renameMap(buildingUuid: Uuid, newDisplayName: String)
}