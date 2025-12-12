package domain.service

import domain.model.Building
import domain.model.Fingerprint
import domain.model.Zone
import kotlin.math.sqrt

class KnnLocationService(
    var k: Int = 3
) : LocationService {
    override fun determineLocation(currentSignals: Fingerprint, building: Building): Zone? {
        if (building.floors.isEmpty() || currentSignals.measurements.isEmpty()) return null

        val buildingZones = building.floors.flatMap { it.zones }
        val zonesFingerprintMap = buildingZones.flatMap { zone ->
            zone.fingerprints.map { fingerprint ->
                zone to fingerprint
            }
        }

        val tagIds = zonesFingerprintMap.flatMap { pair ->
            pair.second.measurements.map { it.tagId }
        }
        val uniqueTagIds = tagIds.toSet()

        val zoneDistances = zonesFingerprintMap.map { (zone, fingerprint) ->
            val distance = euclideanDistance(currentSignals, fingerprint, uniqueTagIds)
            zone to distance
        }

        return zoneDistances
            .sortedBy { (_, distance) -> distance }
            .take(k)
            .groupingBy { (zone, _) -> zone }
            .eachCount()
            .maxByOrNull { it.value }?.key
    }

    private fun euclideanDistance(fp1: Fingerprint, fp2: Fingerprint, tagIds: Set<Int>): Double {
        var sum = 0.0
        for (tagId in tagIds) {
            val rssi1 = fp1.measurements.find { it.tagId == tagId }?.rssi ?: minRssi
            val rssi2 = fp2.measurements.find { it.tagId == tagId }?.rssi ?: minRssi
            sum += (rssi1 - rssi2) * (rssi1 - rssi2)
        }

        return sqrt(sum)
    }

    private val minRssi = -100
}