package data.repository

import data.filesystemProviders.IoFileService
import domain.model.BuildingMap
import domain.model.Floor
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LocalMapRepositoryImplTest {
    class FakeIoFileService : IoFileService {
        private val files = mutableMapOf<String, String>()

        override fun exists(directory: String, fileName: String): Boolean {
            return files.containsKey("$directory/$fileName")
        }

        override suspend fun readFile(directory: String, fileName: String): String {
            val path = "$directory/$fileName"
            return files[path] ?: throw Exception("File not found: $path")
        }

        override suspend fun writeFile(directory: String, fileName: String, content: String) {
            files["$directory/$fileName"] = content
        }

        fun getFileContent(directory: String, fileName: String): String? = files["$directory/$fileName"]
    }

    private val fakeFileIo = FakeIoFileService()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val repository = LocalMapRepositoryImpl(fakeFileIo, json)

    @Test
    fun `getAvailableMapsInfo returns empty list when index does not exist`() = runTest {
        // Given: Empty file system (no index file)
        // When
        val result = repository.getAvailableMapsInfo()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `addMap creates map file and updates index`() = runTest {
        // Given
        val mapId = Uuid.parse("550e8400-e29b-41d4-a716-446655440000")
        val mapName = "My Building"
        val map = createDummyMap(mapId)

        // When
        repository.addMap(mapName, map)

        // Then
        val expectedFileName = "My_Building_$mapId.json"
        assertTrue(
            fakeFileIo.exists("maps", expectedFileName),
            "Map file should exist"
        )

        assertTrue(
            fakeFileIo.exists("maps", "maps_index.json"),
            "Index file should exist"
        )

        val mapsList = repository.getAvailableMapsInfo()
        assertEquals(1, mapsList.size)
        assertEquals(mapName, mapsList[0].name)
        assertEquals(mapId, mapsList[0].id)
    }

    @Test
    fun `getMap returns correct domain model`() = runTest {
        // Given
        val mapId = Uuid.parse("550e8400-e29b-41d4-a716-446655440000")
        val map = createDummyMap(mapId)

        repository.addMap("Test Map", map)

        // When
        val retrievedMap = repository.getMap(mapId)

        // Then
        assertEquals(map.id, retrievedMap.id)
        assertEquals(map.floors.size, retrievedMap.floors.size)
        assertEquals(map.floors[0].name, retrievedMap.floors[0].name)
    }

    @Test
    fun `getMap throws exception when map is not in index`() = runTest {
        // Given
        val randomId = Uuid.parse("550e8400-e29b-41d4-a716-446655440001")

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            repository.getMap(randomId)
        }
    }

    @Test
    fun `addMap updates existing index without overwriting other maps`() = runTest {
        // Given
        val map1 = createDummyMap(Uuid.random())
        val map2 = createDummyMap(Uuid.random())

        repository.addMap("Map 1", map1)

        // When
        repository.addMap("Map 2", map2)

        // Then
        val availableMaps = repository.getAvailableMapsInfo()
        assertEquals(2, availableMaps.size)
        assertTrue(availableMaps.any { it.id == map1.id })
        assertTrue(availableMaps.any { it.id == map2.id })
    }

    private fun createDummyMap(id: Uuid): BuildingMap {
        return BuildingMap(
            id = id,
            floors = listOf(
                Floor(
                    id = Uuid.random(),
                    name = "Ground Floor",
                    walls = emptyList(),
                    zones = emptyList(),
                    pointsOfInterest = emptyList()
                )
            ),
            zoneConnections = emptyList()
        )
    }
}