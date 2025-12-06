package presentation.navigationScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import domain.model.Floor
import domain.model.Node
import domain.model.PointOfInterest
import domain.model.PointOfInterestType
import domain.model.Wall
import domain.model.Zone
import domain.model.ZoneType
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object MapStyles {
    val backgroundColor = Color.LightGray
}

@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold() { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            if (uiState.isLoadingMap) {
                CircularProgressIndicator()
            }

            uiState.map?.let { map ->
                var selectedFloor by remember { mutableStateOf(map.floors.firstOrNull()) }
                selectedFloor?.let { floor ->
                    FloorMap(floor = floor)
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun BuildingMapPreview() {
    val walls = listOf(
        Wall(
            Uuid.random(),
            Node(Uuid.random(), 0.0f, 0.0f),
            Node(Uuid.random(), 500.0f, 0.0f)
        ),
        Wall(
            Uuid.random(),
            Node(Uuid.random(), 500.0f, 0.0f),
            Node(Uuid.random(), 500.0f, -800.0f)
        ),
        Wall(
            Uuid.random(),
            Node(Uuid.random(), 500.0f, -800.0f),
            Node(Uuid.random(), 0.0f, -800.0f)
        ),
    )

    val zones = listOf(
        Zone(
            Uuid.random(),
            "Entrance",
            listOf(
                Node(Uuid.random(), 0.0f, 0.0f),
                Node(Uuid.random(), 500.0f, 0.0f),
                Node(Uuid.random(), 500.0f, -800.0f),
                Node(Uuid.random(), 0.0f, -800.0f)
            ),
            ZoneType.GENERIC
        ),
        Zone(
            Uuid.random(),
            "Bar",
            listOf(
                Node(Uuid.random(), 0.0f, 0.0f),
                Node(Uuid.random(), -500.0f, 0.0f),
                Node(Uuid.random(), -500.0f, -800.0f),
                Node(Uuid.random(), 0.0f, -800.0f)
            ),
            ZoneType.STAIRS
        ),
        Zone(
            Uuid.random(),
            "Secret!",
            listOf(
                Node(Uuid.random(), 0.0f, 0.0f),
                Node(Uuid.random(), 500.0f, 0.0f),
                Node(Uuid.random(), 500.0f, 800.0f),
                Node(Uuid.random(), 0.0f, 800.0f)
            ),
            ZoneType.ELEVATOR
        )
    )

    val pois = listOf(
        PointOfInterest(
            Uuid.random(),
            name = "Shop",
            x = 0f,
            y = 0f,
            type = PointOfInterestType.SHOP
        ),
        PointOfInterest(
            Uuid.random(),
            name = "Generic",
            x = 200f,
            y = 200f,
            type = PointOfInterestType.GENERIC
        ),
        PointOfInterest(
            Uuid.random(),
            name = "Toilet",
            x = -200f,
            y = -200f,
            type = PointOfInterestType.TOILET
        ),
        PointOfInterest(
            id = Uuid.random(),
            name = "Exit",
            x = 200f,
            y = -200f,
            type = PointOfInterestType.EXIT
        ),
        PointOfInterest(
            id = Uuid.random(),
            name = "Restaurant",
            x = -200f,
            y = 200f,
            type = PointOfInterestType.RESTAURANT
        )
    )

    val floor = Floor(
        id = Uuid.random(),
        name = "Piętro 1",
        walls = walls,
        zones = zones,
        pointsOfInterest = pois
    )

    FloorMap(
        floor = floor,
        zones[0],
        zones[1],
        listOf(zones[2])
    )
}

@Composable
fun FloorMap(
    floor: Floor,
    currentZone: Zone? = null,
    selectedZone: Zone? = null,
    pathZones: List<Zone> = emptyList()
) {
    val textMeasurer = rememberTextMeasurer()
    val poiPainters = rememberPoiPainters()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MapStyles.backgroundColor)
        ) {
            val screenCenter = Offset(size.width / 2.0f, size.height / 2.0f)

            withTransform(
                transformBlock = {
                    translate(left = screenCenter.x, top = screenCenter.y)
                }
            ) {
                drawFloor(floor, textMeasurer, currentZone, selectedZone, pathZones)

                floor.pointsOfInterest.forEach { poi ->
                    val color = PoiTheme.get(poi.type).color
                    val iconPainter = poiPainters.getPainter(poi.type)

                    drawPoiPin(poi, textMeasurer, iconPainter, color)
                }
            }
        }
    }
}

fun DrawScope.drawFloor(
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
        drawZone(it, textMeasurer, zoneState)
    }
}

//@Composable
//fun ZoomableMap(
//    floor: Floor,
//    activeZone: Zone? = null,
//) {
//    var rotation by remember { mutableFloatStateOf(0f) }
//    var scale by remember { mutableFloatStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    val textMeasurer = rememberTextMeasurer()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                detectTransformGestures { _, pan, zoom, twist ->
//                    scale *= zoom
//                    scale = scale.coerceIn(0.5f, 5f)
//                    offset += pan
//                    rotation += twist
//                }
//            }
//    ) {
//        Canvas(
//            modifier = Modifier
//                .fillMaxSize()
//                .graphicsLayer {
//                    rotationX = 45f
//                    cameraDistance = 12f * density
//                }
//        ) {
//            val centerX = size.width / 2
//            val centerY = size.height / 2
//
//            withTransform({
//                translate(left = centerX, top = centerY) // Move to composable center
//                translate(left = offset.x, top = offset.y) // Move to panned position
//                rotate(rotation, pivot = Offset.Zero)
//                scale(scale, pivot = Offset.Zero)
//            }) {
//                drawFloor(floor, textMeasurer)
//            }
//        }
//    }
//}