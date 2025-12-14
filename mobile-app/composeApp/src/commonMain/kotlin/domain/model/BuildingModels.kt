package domain.model

import kotlin.jvm.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias TagId = Int
typealias Rssi = Int

@OptIn(ExperimentalUuidApi::class)
data class Building (
    val id: Uuid,
    val floors: List<Floor>,
    val zoneConnections: List<ZoneConnection>
) {
    val connectionsMap: Map<Zone, List<Zone>>
        get() {
            val map = mutableMapOf<Zone, MutableList<Zone>>()
            zoneConnections.forEach { connection ->
                map.getOrPut(connection.zoneA) { mutableListOf() }.add(connection.zoneB)
                map.getOrPut(connection.zoneB) { mutableListOf() }.add(connection.zoneA)
            }
            return map
        }

    val zones: List<Zone>
        get() = floors.flatMap { it.zones }
}

@OptIn(ExperimentalUuidApi::class)
data class Floor (
    val id: Uuid,
    val name: String,
    val walls: List<Wall>,
    val zones: List<Zone>,
    val pointsOfInterest: List<PointOfInterest>
) {
    init {
        zones.forEach { it.floor = this }
    }
}

@OptIn(ExperimentalUuidApi::class)
data class Node(
    val id: Uuid,
    val x: Float,
    val y: Float
)

@OptIn(ExperimentalUuidApi::class)
data class Wall(
    val id: Uuid,
    val start: Node,
    val end: Node
)

enum class ZoneType {
    GENERIC,
    STAIRS,
    ELEVATOR
}

@OptIn(ExperimentalUuidApi::class)
data class Zone(
    val id: Uuid,
    val name: String,
    val boundary: List<Node>,
    val type: ZoneType,
    val fingerprints: MutableList<Fingerprint> = mutableListOf(),
) {
    @Transient
    var floor: Floor? = null

    val centerPos: Pair<Float, Float>
        get() {
            val cornerPoints = boundary

            val minX = cornerPoints.minBy { it.x }.x
            val maxX = cornerPoints.maxBy { it.x }.x
            val minY = cornerPoints.minBy { it.y }.y
            val maxY = cornerPoints.maxBy { it.y }.y

            val centerX = (minX + maxX) / 2
            val centerY = (minY + maxY) / 2

            return Pair(centerX, centerY)
        }
}

data class Fingerprint(
    val measurements: List<Measurement>
)

data class Measurement(
    val tagId: TagId,
    val rssi: Rssi
)

enum class PointOfInterestType {
    GENERIC,
    TOILET,
    SHOP,
    RESTAURANT,
    EXIT,
}

@OptIn(ExperimentalUuidApi::class)
data class PointOfInterest(
    val id: Uuid,
    val name: String,
    val x: Float,
    val y: Float,
    val type: PointOfInterestType
)

data class ZoneConnection(
    val zoneA: Zone,
    val zoneB: Zone
)