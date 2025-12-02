package domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class BuildingMapModel (
    val id: Uuid,
    val floors: List<FloorModel>,
)

@OptIn(ExperimentalUuidApi::class)
data class FloorModel (
    val id: Uuid,
    val name: String,
    val walls: List<WallModel>,
    val zones: List<ZoneModel>,
    val pointsOfInterest: List<PointOfInterestModel>
)

data class Node(val x: Float, val y: Float)

@OptIn(ExperimentalUuidApi::class)
data class WallModel(
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
data class ZoneModel(
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
data class PointOfInterestModel(
    val id: Uuid,
    val name: String,
    val location: Node,
    val type: PointOfInterestType
)