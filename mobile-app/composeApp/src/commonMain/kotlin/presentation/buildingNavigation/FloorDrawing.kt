package presentation.buildingNavigation

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import domain.model.Floor
import domain.model.Zone

fun DrawScope.drawFloorPlan(
    floor: Floor,
    textMeasurer: TextMeasurer,
    currentZone: Zone? = null,
    selectedZone: Zone? = null,
    pathZones: List<Zone> = emptyList()
) {
    floor.walls.forEach {
        drawWall(it)
    }

    floor.zones.forEach {
        val zoneState = when {
            it === selectedZone -> ZoneState.SELECTED
            it === currentZone -> ZoneState.CURRENT
            it in pathZones -> ZoneState.PATH
            else -> ZoneState.NONE
        }
        drawZone(it, zoneState)
    }

    floor.zones.forEach {
        drawZoneLabel(it, textMeasurer)
    }
}