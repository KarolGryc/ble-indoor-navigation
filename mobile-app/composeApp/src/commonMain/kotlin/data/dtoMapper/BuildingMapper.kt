package data.mapper

import data.dto.BuildingMapDto
import data.dto.FloorDto
import data.dto.NodeDto
import data.dto.PointOfInterestDto
import data.dto.WallDto
import data.dto.ZoneConnectionDto
import data.dto.ZoneDto
import domain.model.BuildingMap
import domain.model.Floor
import domain.model.Node
import domain.model.PointOfInterest
import domain.model.PointOfInterestType
import domain.model.Wall
import domain.model.Zone
import domain.model.ZoneConnection
import domain.model.ZoneType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object BuildingMapper {
    fun mapToDomain(dto: BuildingMapDto): BuildingMap {
        val mappedFloors = dto.floors.map { mapFloorToDomain(it) }

        val allZonesMap: Map<Uuid, Zone> = mappedFloors
            .flatMap { it.zones }
            .associateBy { it.id }

        val mappedConnections = dto.zoneConnections.mapNotNull { connDto ->
            val zoneA = allZonesMap[connDto.zone1Id]
            val zoneB = allZonesMap[connDto.zone2Id]

            if (zoneA != null && zoneB != null) {
                ZoneConnection(zoneA, zoneB)
            } else {
                null
            }
        }

        return BuildingMap(
            id = dto.id,
            floors = mappedFloors,
            zoneConnections = mappedConnections
        )
    }

    fun mapToDto(domain: BuildingMap): BuildingMapDto {
        val floorsDto = domain.floors.map { mapFloorToDto(it) }

        val connectionsDto = domain.zoneConnections.map { conn ->
            ZoneConnectionDto(
                zone1Id = conn.zoneA.id,
                zone2Id = conn.zoneB.id
            )
        }

        return BuildingMapDto(
            id = domain.id,
            floors = floorsDto,
            zoneConnections = connectionsDto
        )
    }

    private fun mapFloorToDomain(dto: FloorDto): Floor {
        val nodesMap: Map<Uuid, Node> = dto.nodes.associate { nodeDto ->
            nodeDto.id to Node(
                id = nodeDto.id,
                x = nodeDto.x,
                y = nodeDto.y
            )
        }

        fun getNode(id: Uuid): Node {
            val message = "Node with id $id not found in floor ${dto.id}"
            return nodesMap[id] ?: throw IllegalStateException(message)
        }

        val walls = dto.walls.map { wallDto ->
            Wall(
                id = wallDto.id,
                start = getNode(wallDto.startNodeId),
                end = getNode(wallDto.endNodeId)
            )
        }

        val zones = dto.zones.map { zoneDto ->
            Zone(
                id = zoneDto.id,
                name = zoneDto.name,
                boundary = zoneDto.cornerNodeIds.map { getNode(it) },
                type = ZoneType.valueOf(zoneDto.type)
            )
        }

        val pois = dto.pointsOfInterest.map { poiDto ->
            PointOfInterest(
                id = poiDto.id,
                name = poiDto.name,
                x = poiDto.x,
                y = poiDto.y,
                type = PointOfInterestType.valueOf(poiDto.type)
            )
        }

        return Floor(
            id = dto.id,
            name = dto.name,
            walls = walls,
            zones = zones,
            pointsOfInterest = pois
        )
    }

    private fun mapFloorToDto(floor: Floor): FloorDto {
        val usedNodes = mutableSetOf<Node>()

        floor.walls.forEach {
            usedNodes.add(it.start)
            usedNodes.add(it.end)
        }

        floor.zones.forEach { zone ->
            usedNodes.addAll(zone.boundary)
        }

        val nodesDto = usedNodes.map { node ->
            NodeDto(
                id = node.id,
                x = node.x,
                y = node.y
            )
        }

        val wallsDto = floor.walls.map { wall ->
            WallDto(
                id = wall.id,
                startNodeId = wall.start.id,
                endNodeId = wall.end.id
            )
        }

        val zonesDto = floor.zones.map { zone ->
            ZoneDto(
                id = zone.id,
                name = zone.name,
                type = zone.type.name,
                cornerNodeIds = zone.boundary.map { it.id }
            )
        }

        val poisDto = floor.pointsOfInterest.map { poi ->
            PointOfInterestDto(
                id = poi.id,
                name = poi.name,
                x = poi.x,
                y = poi.y,
                type = poi.type.name
            )
        }

        return FloorDto(
            id = floor.id,
            name = floor.name,
            nodes = nodesDto,
            walls = wallsDto,
            zones = zonesDto,
            pointsOfInterest = poisDto
        )
    }
}