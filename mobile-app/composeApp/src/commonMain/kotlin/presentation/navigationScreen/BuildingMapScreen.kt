package presentation.navigationScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import domain.model.Floor
import domain.model.PointOfInterest
import domain.model.PointOfInterestType
import domain.model.Wall
import domain.model.Zone
import domain.model.ZoneType

object MapStyles {
    val WallColor = Color.Black
    const val WALL_THICKNESS = 5f
    val ZoneColor = Color.LightGray.copy(alpha = 0.3f)
    val ZoneBorderColor = Color.Gray
    val POIColor = Color.Blue
    const val POI_RADIUS = 10f
}

@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    Scaffold() { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isLoadingMap) {
                CircularProgressIndicator()
            }

            uiState.map?.let { map ->
                BuildingMapScreen(
                    map.floors
                )
            }
        }
    }
}

@Composable
fun BuildingMapScreen(
    floors: List<Floor>
) {
    var selectedFloor by remember { mutableStateOf(floors.firstOrNull()) }

    selectedFloor?.let { floor ->
        ZoomableMap(floor = floor)
    }
}

@Composable
fun ZoomableMap(floor: Floor) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    scale = scale.coerceIn(0.5f, 5f)

                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(left = offset.x, top = offset.y)
                scale(scale, pivot = Offset.Zero)
            }) {
                drawFloor(floor)
            }
        }
    }
}


fun DrawScope.drawFloor(floor: Floor) {
    floor.zones.forEach { zone ->
        drawZone(zone)
    }

    floor.walls.forEach { wall ->
        drawWall(wall)
    }

    floor.pointsOfInterest.forEach { poi ->
        drawPoi(poi)
    }
}

fun DrawScope.drawWall(wall: Wall) {
    drawLine(
        color = MapStyles.WallColor,
        start = Offset(wall.start.x, wall.start.y),
        end = Offset(wall.end.x, wall.end.y),
        strokeWidth = MapStyles.WALL_THICKNESS
    )
}

fun DrawScope.drawZone(zone: Zone) {
    if (zone.boundary.isEmpty()) return

    val path = Path().apply {
        moveTo(zone.boundary.first().x, zone.boundary.first().y)
        for (i in 1 until zone.boundary.size) {
            lineTo(zone.boundary[i].x, zone.boundary[i].y)
        }
        close()
    }

    drawPath(
        path = path,
        color = when (zone.type) {
            ZoneType.STAIRS -> Color.Yellow.copy(alpha = 0.3f)
            ZoneType.ELEVATOR -> Color.Red.copy(alpha = 0.3f)
            else -> MapStyles.ZoneColor
        }
    )

    drawPath(
        path = path,
        color = MapStyles.ZoneBorderColor,
        style = Stroke(width = 2f)
    )
}

fun DrawScope.drawPoi(poi: PointOfInterest) {
    val color = when (poi.type) {
        PointOfInterestType.TOILET -> Color.Cyan
        PointOfInterestType.EXIT -> Color.Green
        else -> MapStyles.POIColor
    }

    drawCircle(
        color = color,
        radius = MapStyles.POI_RADIUS,
        center = Offset(poi.x, poi.y)
    )
}