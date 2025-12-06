package domain.repository

import domain.model.BuildingMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BuildingMapRepository {
    suspend fun getMapsInfo(): List<MapInfo>
    suspend fun getMap(buildingUuid: Uuid): BuildingMap
    suspend fun addMap(name: String, building: BuildingMap)
    suspend fun removeMap(buildingUuid: Uuid)
    suspend fun renameMap(buildingUuid: Uuid, newDisplayName: String)
}