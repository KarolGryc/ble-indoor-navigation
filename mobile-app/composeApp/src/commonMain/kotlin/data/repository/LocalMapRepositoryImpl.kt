package data.repository

import data.filesystemProviders.IoFileService
import domain.model.BuildingMapModel
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LocalMapRepositoryImpl(
    private val fileIO: IoFileService
) : BuildingMapRepository {

    companion object {
        const val MAPS_DIRECTORY = "maps"
        const val MAP_FILE_EXTENSION = ".map"
    }

    override suspend fun getAvailableMapsInfo(): List<MapInfo> {
        val availableFiles = fileIO.listFiles(directory = MAPS_DIRECTORY, MAP_FILE_EXTENSION)
        TODO("Not yet implemented")
    }

    override suspend fun getMap(buildingUuid: Uuid): BuildingMapModel {
        TODO("Not yet implemented")
    }

    override suspend fun saveMap(buildingMap: BuildingMapModel) {
        TODO("Not yet implemented")
    }
}