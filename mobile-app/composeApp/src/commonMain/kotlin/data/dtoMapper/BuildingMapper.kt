package data.dtoMapper

import data.dto.BuildingMapDto
import data.dto.FingerprintDto
import data.dto.FloorDto
import data.dto.MeasurementDto
import data.dto.NodeDto
import data.dto.PointOfInterestDto
import data.dto.WallDto
import data.dto.ZoneConnectionDto
import data.dto.ZoneDto
import domain.model.Building
import domain.model.Fingerprint
import domain.model.Floor
import domain.model.Measurement
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
    fun mapToDomain(dto: BuildingMapDto): Building {
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

        return Building(
            id = dto.id,
            floors = mappedFloors,
            zoneConnections = mappedConnections
        )
    }

    fun mapToDto(domain: Building): BuildingMapDto {
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

        val wallsDto = dto.walls.map { wallDto ->
            Wall(
                id = wallDto.id,
                start = getNode(wallDto.startNodeId),
                end = getNode(wallDto.endNodeId)
            )
        }

        val zonesDto = dto.zones.map { zoneDto ->
            Zone(
                id = zoneDto.id,
                name = zoneDto.name,
                boundary = zoneDto.cornerNodeIds.map { getNode(it) },
                type = ZoneType.valueOf(zoneDto.type),
                fingerprints = mapFingerprintsToDomain(zoneDto.fingerprints).toMutableList()
            )
        }

        val poisDto = dto.pointsOfInterest.map { poiDto ->
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
            walls = wallsDto,
            zones = zonesDto,
            pointsOfInterest = poisDto
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

        val nodesDtos = usedNodes.map { nodeDto ->
            NodeDto(
                id = nodeDto.id,
                x = nodeDto.x,
                y = nodeDto.y
            )
        }

        val wallsDtos = floor.walls.map { wallDto ->
            WallDto(
                id = wallDto.id,
                startNodeId = wallDto.start.id,
                endNodeId = wallDto.end.id
            )
        }

        val zonesDtos = floor.zones.map { zoneDto ->
            ZoneDto(
                id = zoneDto.id,
                name = zoneDto.name,
                type = zoneDto.type.name,
                cornerNodeIds = zoneDto.boundary.map { it.id },
                fingerprints = mapFingerprintsToDto(zoneDto.fingerprints)
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
            nodes = nodesDtos,
            walls = wallsDtos,
            zones = zonesDtos,
            pointsOfInterest = poisDto
        )
    }

    private fun mapFingerprintsToDomain(fingerprintsDtos: List<FingerprintDto>): List<Fingerprint> {
        return fingerprintsDtos.map { fingerprintDto ->
            val measurements = fingerprintDto.measurements.map { measurementDto ->
                Measurement(
                    tagId = measurementDto.tagId,
                    rssi = measurementDto.rssi
                )
            }

            Fingerprint(
                measurements = measurements
            )
        }
    }

    private fun mapFingerprintsToDto(fingerprints: List<Fingerprint>) : List<FingerprintDto> {
        return fingerprints.map { fingerprint ->
            val measurementsDtos = fingerprint.measurements.map { measurement ->
                MeasurementDto(
                    tagId = measurement.tagId,
                    rssi = measurement.rssi
                )
            }

            FingerprintDto(
                measurements = measurementsDtos
            )
        }
    }
}