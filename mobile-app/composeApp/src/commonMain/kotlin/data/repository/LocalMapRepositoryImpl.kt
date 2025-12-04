package data.repository

import data.dto.BuildingMapDto
import data.dto.UuidSerializer
import data.filesystemProviders.IoFileService
import data.mapper.BuildingMapper
import domain.model.BuildingMap
import domain.repository.BuildingMapRepository
import domain.repository.MapInfo
import kotlinx.serialization.Serializable
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

        val mapJson = fileIO.readFile(MAPS_DIRECTORY, mapEntry.name)
        val mapDto = json.decodeFromString<BuildingMapDto>(mapJson)

        return BuildingMapper.mapToDomain(mapDto)
    }

    override suspend fun addMap(name: String, buildingMap: BuildingMap) {
        val buildingDto = BuildingMapper.mapToDto(buildingMap)
        val buildingJson = json.encodeToString(buildingDto)

        val safeFileName = getSafeFileName(name, buildingMap.id)
        fileIO.writeFile(MAPS_DIRECTORY, safeFileName, buildingJson)

        val newEntry = MapIndexEntry(
            id = buildingMap.id,
            name = safeFileName,
            displayName = name
        )
        updateIndex(entry = newEntry)
    }

    override suspend fun removeMap(buildingUuid: Uuid) {
        val indexEntries = loadIndex()
        val mapEntry = indexEntries.find { it.id == buildingUuid }
            ?: throw IllegalArgumentException("Map with ID $buildingUuid not found")

        fileIO.deleteFile(MAPS_DIRECTORY, mapEntry.name)

        deleteEntryFromIndex(buildingUuid)
    }

    private suspend fun deleteEntryFromIndex(buildingUuid: Uuid) {
        val indexEntries = loadIndex()
        val updatedEntries = indexEntries.filter { it.id != buildingUuid }
        updateIndex(updatedEntries = updatedEntries)
    }

    private suspend fun updateIndex(entry: MapIndexEntry) {
        updateIndex(listOf(entry))
    }

    private suspend fun updateIndex(updatedEntries: List<MapIndexEntry>) {
        val filesIndex = loadIndex().toMutableList()
        filesIndex.clear()
        filesIndex.addAll(updatedEntries)

        fileIO.writeFile(
            directory = MAPS_DIRECTORY,
            fileName = INDEX_FILE_NAME,
            content = json.encodeToString(filesIndex)
        )
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

    private fun getSafeFileName(name: String, id: Uuid): String {
        val replacedName = name.replace(" ", "_")
        return "${replacedName}_$id.$MAP_FILE_EXTENSION"
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