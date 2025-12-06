package presentation.navigationScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val wallColor = Color.Black
    val wallThickness = 5f

    val zoneOpacity = 0.5f
    val zoneNameSize = 16.sp
    val zoneNameColor = Color.White
    val zoneNameStrokeColor = Color.Black
    val zoneStrokeWidth = 6f
}

enum class ZoneState(val color: Color) {
    NONE(Color(0xADFFFFFF)),
    SELECTED(Color(0xffff0000)),
    CURRENT(Color(0xff2e75ff)),
    PATH(Color(0xffffa46b))
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

    val defaultIcon = rememberVectorPainter(Icons.Default.Explore)
    val exitIcon = rememberVectorPainter(Icons.Default.DoorFront)
    val wcIcon = rememberVectorPainter(Icons.Default.Wc)
    val shopIcon = rememberVectorPainter(Icons.Default.ShoppingBasket)
    val restaurantIcon = rememberVectorPainter(Icons.Default.Restaurant)

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

                    val (icon, color) = when (poi.type) {
                        PointOfInterestType.GENERIC -> defaultIcon to Color(0xFFD41F1F)
                        PointOfInterestType.TOILET -> wcIcon to Color(0xFF2166E5)
                        PointOfInterestType.SHOP -> shopIcon to Color(0xFFEE6A52)
                        PointOfInterestType.RESTAURANT -> restaurantIcon to Color(0xFFF5B400)
                        PointOfInterestType.EXIT -> exitIcon to Color(0xFF2A7F00)
                    }

                    drawPoiPin(
                        poi = poi,
                        iconPainter = icon,
                        color = color
                    )
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
    floor.zones.forEach {
        val zoneState = when {
            it === selectedZone -> ZoneState.SELECTED
            it === currentZone -> ZoneState.CURRENT
            it in pathZones -> ZoneState.PATH
            else -> ZoneState.NONE
        }
        drawZone(it, textMeasurer, zoneState)
    }
    
    floor.walls.forEach {
        drawWall(it)
    }
}

fun DrawScope.drawWall(wall: Wall) {
    drawLine(
        color = MapStyles.wallColor,
        start = Offset(wall.start.x, wall.start.y),
        end = Offset(wall.end.x, wall.end.y),
        strokeWidth = MapStyles.wallThickness
    )
}

fun DrawScope.drawZone(
    zone: Zone,
    textMeasurer: TextMeasurer,
    state: ZoneState = ZoneState.NONE
) {
    val cornerPoints = zone.boundary
    if (cornerPoints.isEmpty()) {
        return
    }

    val path = Path().apply {
        val startPoint = cornerPoints.first()
        moveTo(startPoint.x, startPoint.y)

        for (i in 1..< cornerPoints.size) {
            val pos = cornerPoints[i]
            lineTo(pos.x, pos.y)
        }
        close()
    }

    val zoneColor = state.color
    drawPath(path = path, color = zoneColor, alpha = MapStyles.zoneOpacity)

    val minX = cornerPoints.minBy { it.x }.x
    val maxX = cornerPoints.maxBy { it.x }.x
    val minY = cornerPoints.minBy { it.y }.y
    val maxY = cornerPoints.maxBy { it.y }.y

    val centerX = (minX + maxX) / 2
    val centerY = (minY + maxY) / 2

    val baseStyle = TextStyle(
        fontSize = MapStyles.zoneNameSize,
        fontWeight = FontWeight.Medium,
        color = Color.Unspecified
    )

    val nameEmote = when(zone.type) {
        ZoneType.STAIRS -> "\uD80C\uDE8D"
        ZoneType.ELEVATOR -> "\uD83D\uDED7"
        else -> ""
    }
    val textLayoutResult = textMeasurer.measure(text = "${zone.name} $nameEmote", style = baseStyle)
    val textOffset = Offset(
        x = centerX - textLayoutResult.size.width / 2,
        y = centerY - textLayoutResult.size.height / 2
    )

    drawText(
        textLayoutResult = textLayoutResult,
        color = MapStyles.zoneNameStrokeColor,
        topLeft = textOffset,
        drawStyle = Stroke(width = MapStyles.zoneStrokeWidth)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        color = MapStyles.zoneNameColor,
        topLeft = textOffset,
        drawStyle = Fill
    )
}

fun DrawScope.drawPoiPin(
    poi: PointOfInterest,
    iconPainter: VectorPainter,
    color: Color = Color.Red,
    borderColor: Color = Color.White,
    iconColor: Color = Color.White
) {
    val pinRadius = 24.dp.toPx()
    val pinHeight = 60.dp.toPx()
    val iconSize = 24.dp.toPx()
    
    val tip = Offset(poi.x, poi.y)
    val pinPath = createPinPath(tip, pinRadius, pinHeight)
    translate(left = 2f, top = 2f) {
        drawPath(
            path = pinPath,
            color = Color.Black.copy(alpha = 0.3f)
        )
    }

    drawPath(
        path = pinPath,
        color = color
    )

    drawPath(
        path = pinPath,
        color = borderColor,
        style = Stroke(width = 3.dp.toPx())
    )

    val headCenterY = poi.y - pinHeight + pinRadius

    translate(
        left = poi.x - iconSize / 2,
        top = headCenterY - iconSize / 2
    ) {
        with(iconPainter) {
            draw(
                size = Size(iconSize, iconSize),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}

private fun createPinPath(tipPosition: Offset, radius: Float, height: Float): Path {
    return Path().apply {
        moveTo(tipPosition.x, tipPosition.y)
        
        val centerX = tipPosition.x
        val centerY = tipPosition.y - height + radius

        val stemHeight = height - radius
        val controlPointY = tipPosition.y - (stemHeight * 0.6f)
        quadraticTo(
            x1 = centerX - radius, y1 = controlPointY,
            x2 = centerX - radius, y2 = centerY
        )

        arcTo(
            rect = Rect(
                left = centerX - radius,
                top = centerY - radius,
                right = centerX + radius,
                bottom = centerY + radius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        quadraticTo(
            x1 = centerX + radius, y1 = controlPointY,
            x2 = tipPosition.x, y2 = tipPosition.y
        )
        close()
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
//
//fun DrawScope.drawFloorBase(floor: Floor, textMeasurer: TextMeasurer) {
//    floor.zones.forEach { zone ->
//        drawZone(zone, textMeasurer)
//    }
//    floor.walls.forEach { wall ->
//        drawWall(wall)
//    }
//}
//
//fun DrawScope.drawWall(wall: Wall) {
//    drawLine(
//        color = MapStyles.WallColor,
//        start = Offset(wall.start.x, wall.start.y),
//        end = Offset(wall.end.x, wall.end.y),
//        strokeWidth = MapStyles.WALL_THICKNESS
//    )
//}
//
//fun DrawScope.drawZone(zone: Zone, textMeasurer: TextMeasurer) {
//    if (zone.boundary.isEmpty()) return
//
//    val path = Path().apply {
//        moveTo(zone.boundary.first().x, zone.boundary.first().y)
//        for (i in 1 until zone.boundary.size) {
//            lineTo(zone.boundary[i].x, zone.boundary[i].y)
//        }
//        close()
//    }
//
//    drawPath(
//        path = path,
//        color = when (zone.type) {
//            ZoneType.STAIRS -> Color.Yellow.copy(alpha = 0.3f)
//            ZoneType.ELEVATOR -> Color.Red.copy(alpha = 0.3f)
//            else -> MapStyles.ZoneColor
//        }
//    )
//
//    drawPath(
//        path = path,
//        color = MapStyles.ZoneBorderColor,
//        style = Stroke(width = 2f)
//    )
//
//    val centerX = zone.boundary.map { it.x }.average().toFloat()
//    val centerY = zone.boundary.map { it.y }.average().toFloat()
//
//    val textStyle = TextStyle(
//        color = Color.Black,
//        fontSize = 14.sp,
//        fontWeight = FontWeight.Medium
//    )
//
//    val textLayoutResult = textMeasurer.measure(
//        text = zone.name,
//        style = textStyle
//    )
//
//    drawText(
//        textLayoutResult = textLayoutResult,
//        topLeft = Offset(
//            x = centerX - textLayoutResult.size.width / 2,
//            y = centerY - textLayoutResult.size.height / 2
//        )
//    )
//}
//
//fun DrawScope.drawPoi(poi: PointOfInterest) {
//    val color = when (poi.type) {
//        PointOfInterestType.TOILET -> Color.Cyan
//        PointOfInterestType.EXIT -> Color.Green
//        else -> MapStyles.POIColor
//    }
//
//    drawCircle(
//        color = color,
//        radius = MapStyles.POI_RADIUS,
//        center = Offset(poi.x, poi.y)
//    )
//}