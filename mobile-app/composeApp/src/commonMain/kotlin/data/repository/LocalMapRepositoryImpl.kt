package data.repository

import data.dto.BuildingMapDto
import data.dto.UuidSerializer
import data.filesystemProviders.IoFileService
import data.mapper.BuildingMapper
import domain.model.BuildingMap
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LocalMapRepositoryImpl(
    private val fileIO: IoFileService,
    private val json: Json = Json { prettyPrint = true; ignoreUnknownKeys = true }
) : BuildingMapRepository {
    companion object {
        const val MAPS_DIRECTORY = "maps"
        const val MAP_FILE_EXTENSION = "json"
        const val INDEX_FILE_NAME = "maps_index.json"
    }

    override suspend fun getAvailableMapsInfo(): List<MapInfo> {
        if (!fileIO.exists(MAPS_DIRECTORY, INDEX_FILE_NAME)) {
            return emptyList()
        }

        val indexJson = fileIO.readFile(MAPS_DIRECTORY, INDEX_FILE_NAME)
        val indexEntries = json.decodeFromString<List<MapIndexEntry>>(indexJson)

        return indexEntries.map { entry ->
            MapInfo(id = entry.id, name = entry.displayName)
        }
    }

    override suspend fun getMap(buildingUuid: Uuid): BuildingMap {
        val indexEntries = loadIndex()
        val mapEntry = indexEntries.find { it.id == buildingUuid }
            ?: throw IllegalArgumentException("Map with ID $buildingUuid not found")

        val fileName = "${mapEntry.name}.$MAP_FILE_EXTENSION"
        val mapJson = fileIO.readFile(MAPS_DIRECTORY, fileName)
        val mapDto = json.decodeFromString<BuildingMapDto>(mapJson)

        return BuildingMapper.mapToDomain(mapDto)
    }

    override suspend fun addMap(name: String, buildingMap: BuildingMap) {
        val mapDto = BuildingMapper.mapToDto(buildingMap)
        val mapJson = json.encodeToString(mapDto)

        val replacedName = name.replace(" ", "_")
        val buildingId = buildingMap.id
        val safeFileName = "${replacedName}_$buildingId.$MAP_FILE_EXTENSION"
        fileIO.writeFile(MAPS_DIRECTORY, safeFileName, mapJson)

        val currentIndex = loadIndex().toMutableList()
        currentIndex.removeAll { it.id == buildingMap.id }
        currentIndex.add(
            MapIndexEntry(id = buildingMap.id, name = safeFileName, displayName = name)
        )

        val newIndexJson = json.encodeToString(currentIndex)
        fileIO.writeFile(MAPS_DIRECTORY, INDEX_FILE_NAME, newIndexJson)
    }

    private suspend fun loadIndex(): List<MapIndexEntry> {
        if (!fileIO.exists(MAPS_DIRECTORY, INDEX_FILE_NAME)) {
            return emptyList()
        }

        val indexJson = fileIO.readFile(MAPS_DIRECTORY, INDEX_FILE_NAME)
        return try {
            json.decodeFromString<List<MapIndexEntry>>(indexJson)
        } catch (_: Exception) { emptyList() }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Serializable
    data class MapIndexEntry (
        @Serializable(with = UuidSerializer::class)
        val id: Uuid,
        val name: String,
        val displayName: String
    )
}