
import domain.model.Building
import domain.model.Fingerprint
import domain.model.Floor
import domain.model.Measurement
import domain.model.Zone
import domain.model.ZoneType
import domain.usecase.DetermineLocationUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DetermineLocationUseCaseTest {

    @Test
    fun `should return null when building has no floors`() {
        // Given
        val emptyBuilding = createBuilding(floors = emptyList())
        val signal = createFingerprint(1 to -50)

        // When
        val useCase = DetermineLocationUseCase(k = 3)
        val result = useCase(signal, emptyBuilding)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when current signal measurements are empty`() {
        // Given
        val zone = createZone("Zone A")
        zone.fingerprints.add(createFingerprint(1 to -50))
        val building = createBuildingWithZones(listOf(zone))

        val emptySignal = Fingerprint(emptyList())

        // When
        val useCase = DetermineLocationUseCase(k = 3)
        val result = useCase(emptySignal, building)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return correct zone for exact match`() {
        // Given
        val zoneA = createZone("Zone A")
        val zoneB = createZone("Zone B")

        zoneA.fingerprints.add(createFingerprint(1 to -50))
        zoneB.fingerprints.add(createFingerprint(1 to -90))

        val building = createBuildingWithZones(listOf(zoneA, zoneB))
        val currentSignal = createFingerprint(1 to -50)

        // When
        val useCase = DetermineLocationUseCase(k = 1)
        val result = useCase(currentSignal, building)

        // Then
        assertEquals(zoneA, result)
    }

    @Test
    fun `should select zone based on majority vote (k=3 logic)`() {
        // Given
        val zoneA = createZone("Zone A")
        val zoneB = createZone("Zone B")

        zoneA.fingerprints.add(createFingerprint(1 to -50))
        zoneA.fingerprints.add(createFingerprint(1 to -70))

        zoneB.fingerprints.add(createFingerprint(1 to -60))
        zoneB.fingerprints.add(createFingerprint(1 to -90))
        zoneB.fingerprints.add(createFingerprint(1 to -40))

        val building = createBuildingWithZones(listOf(zoneA, zoneB))
        val currentSignal = createFingerprint(1 to -60)

        // When
        val useCase = DetermineLocationUseCase(k = 3)
        val result = useCase(currentSignal, building)

        // Then
        assertEquals(zoneA, result, "Should choose Zone A because it has 2/3 closest neighbors")
    }

    @Test
    fun `should handle missing tags correctly (penalty check)`() {
        // Given
        val zoneA = createZone("Zone A")
        zoneA.fingerprints.add(createFingerprint(1 to -50))

        val zoneB = createZone("Zone B")
        zoneB.fingerprints.add(createFingerprint(2 to -50))

        val building = createBuildingWithZones(listOf(zoneA, zoneB))
        val currentSignal = createFingerprint(1 to -50)

        // When
        val useCase = DetermineLocationUseCase(k = 1)
        val result = useCase(currentSignal, building)

        // Then
        assertEquals(zoneA, result)
    }

    // --- Helpers ---

    private fun createFingerprint(vararg signals: Pair<Int, Int>): Fingerprint {
        val measurements = signals.map { (tagId, rssi) ->
            Measurement(tagId = tagId, rssi = rssi)
        }
        return Fingerprint(measurements)
    }

    private fun createZone(name: String): Zone {
        return Zone(
            id = Uuid.random(),
            name = name,
            boundary = emptyList(),
            type = ZoneType.GENERIC,
            fingerprints = mutableListOf()
        )
    }

    private fun createBuildingWithZones(zones: List<Zone>): Building {
        val floor = Floor(
            id = Uuid.random(),
            name = "Ground Floor",
            walls = emptyList(),
            zones = zones,
            pointsOfInterest = emptyList()
        )
        return createBuilding(listOf(floor))
    }

    private fun createBuilding(floors: List<Floor>): Building {
        return Building(
            id = Uuid.random(),
            floors = floors,
            zoneConnections = emptyList()
        )
    }
}