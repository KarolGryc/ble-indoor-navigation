package data.repository

import data.filesystemProviders.IoFileService
import domain.model.BuildingMapModel
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LocalMapRepositoryImpl(
    private val fileDataSource: IoFileService
) : BuildingMapRepository{

    override suspend fun getAvailableMapsInfo(): List<MapInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun getMap(buildingUuid: Uuid): BuildingMapModel {
        TODO("Not yet implemented")
    }

    override suspend fun saveMap(buildingMap: BuildingMapModel) {
        TODO("Not yet implemented")
    }
}