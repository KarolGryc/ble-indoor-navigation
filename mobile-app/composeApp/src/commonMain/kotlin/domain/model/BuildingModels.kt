package domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class BuildingMap (
    val id: Uuid,
    val floors: List<Floor>,
    val zoneConnections: List<ZoneConnection>
)

@OptIn(ExperimentalUuidApi::class)
data class Floor (
    val id: Uuid,
    val name: String,
    val walls: List<Wall>,
    val zones: List<Zone>,
    val pointsOfInterest: List<PointOfInterest>
)

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
    val type: ZoneType
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