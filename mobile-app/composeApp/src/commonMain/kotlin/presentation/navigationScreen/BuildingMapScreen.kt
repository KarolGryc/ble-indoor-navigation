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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
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
                BuildingMap(
                    map.floors
                )
            }
        }
    }
}

@Composable
fun BuildingMap(
    floors: List<Floor>
) {
    var selectedFloor by remember { mutableStateOf(floors.firstOrNull()) }

    selectedFloor?.let { floor ->
        ZoomableMap(floor = floor)
    }
}



@Composable
fun ZoomableMap(floor: Floor) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, twist ->
                    scale *= zoom
                    scale = scale.coerceIn(0.5f, 5f)
                    offset += pan
                    rotation += twist
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
//                .graphicsLayer {
//                    rotationX = 45f
//                    cameraDistance = 12f * density
//                }
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            withTransform({
                translate(left = centerX, top = centerY) // Move to composable center
                translate(left = offset.x, top = offset.y) // Move to panned position
                rotate(rotation, pivot = Offset.Zero)
                scale(scale, pivot = Offset.Zero)
            }) {
                drawFloor(floor, textMeasurer)
            }
        }
    }
}


fun DrawScope.drawFloor(floor: Floor, textMeasurer: TextMeasurer) {
    floor.zones.forEach { zone ->
        drawZone(zone, textMeasurer)
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

fun DrawScope.drawZone(zone: Zone, textMeasurer: TextMeasurer) {
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

    val centerX = zone.boundary.map { it.x }.average().toFloat()
    val centerY = zone.boundary.map { it.y }.average().toFloat()

    val textStyle = TextStyle(
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    val textLayoutResult = textMeasurer.measure(
        text = zone.name,
        style = textStyle
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x = centerX - textLayoutResult.size.width / 2,
            y = centerY - textLayoutResult.size.height / 2
        )
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