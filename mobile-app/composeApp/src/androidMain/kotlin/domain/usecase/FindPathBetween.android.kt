package domain.usecase

import domain.model.Building
import domain.model.Zone
import java.util.PriorityQueue
import kotlin.math.sqrt

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FindPathBetweenUseCase {
    actual operator fun invoke(building: Building, startZone: Zone, endZone: Zone): FoundPath? {
        val zones = building.zones
        if (!zones.contains(startZone) || !zones.contains(endZone)) {
            return null
        }

        val connections = building.connectionsMap

        val prevZones = mutableMapOf<Zone, Zone>()

        val fScore = mutableMapOf<Zone, Float>().withDefault { Float.MAX_VALUE }
        fScore[startZone] = distance(startZone, endZone)

        val gScore = mutableMapOf<Zone, Float>().withDefault { Float.MAX_VALUE }
        gScore[startZone] = 0.0f

        val pq = PriorityQueue<Zone> { z1, z2 ->
            val f1 = fScore[z1] ?: Float.MAX_VALUE
            val f2 = fScore[z2] ?: Float.MAX_VALUE
            f1.compareTo(f2)
        }
        pq.add(startZone)

        while(pq.isNotEmpty()) {
            val current = pq.poll() ?: break

            if (current == endZone) {
                val path = reconstructPath(prevZones, current)
                return FoundPath(path)
            }

            val neighbors = connections[current] ?: emptyList()
            for (neighbor in neighbors) {
                val estimatedG = (gScore[current] ?: Float.MAX_VALUE) + distance(current, neighbor)
                if (estimatedG < (gScore[neighbor] ?: Float.MAX_VALUE)) {
                    prevZones[neighbor] = current
                    gScore[neighbor] = estimatedG
                    fScore[neighbor] = estimatedG + distance(neighbor, endZone)
                    if (!pq.contains(neighbor)) {
                        pq.add(neighbor)
                    }
                }
            }
        }

        return null
    }

    private fun reconstructPath(prevZones: Map<Zone, Zone>, current: Zone): List<Zone> {
        val path = mutableListOf<Zone>()
        var zone: Zone? = current
        while (zone != null) {
            path.add(0, zone)
            zone = prevZones[zone]
        }
        return path
    }

    private fun distance(zoneA: Zone, zoneB: Zone): Float {
        val isFloorChange = zoneA.floor != zoneB.floor

        val (ax, ay) = zoneA.centerPos
        val (bx, by) = zoneB.centerPos
        val dx = ax - bx
        val dy = ay - by
        val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        return if (isFloorChange) dist * 10f else dist
    }
}