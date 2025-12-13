package presentation.buildingNavigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import domain.model.Floor
import domain.model.Node
import domain.model.PointOfInterest
import domain.model.PointOfInterestType
import domain.model.Wall
import domain.model.Zone
import domain.model.ZoneType
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun FloorMapPreview() {
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
        PointOfInterest(id = Uuid.random(), name = "Shop", x = 0f, y = 0f, type = PointOfInterestType.SHOP),
        PointOfInterest(Uuid.random(), name = "Generic", x = 200f, y = 200f, type = PointOfInterestType.GENERIC),
        PointOfInterest(Uuid.random(), name = "Toilet", x = -200f, y = -200f, type = PointOfInterestType.TOILET),
        PointOfInterest(id = Uuid.random(), name = "Exit", x = 200f, y = -200f, type = PointOfInterestType.EXIT),
        PointOfInterest(id = Uuid.random(), name = "Restaurant", x = -200f, y = 200f, type = PointOfInterestType.RESTAURANT)
    )

    val floor = Floor(id = Uuid.random(), name = "Piętro 1", walls = walls, zones = zones, pointsOfInterest = pois)

    FloorMap(
        floor = floor,
        cameraState = ViewportState(),
        zones[0],
        zones[1],
        listOf(zones[2])
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun FloorMap(
    floor: Floor?,
    cameraState: ViewportState,
    currentZone: Zone? = null,
    selectedZone: Zone? = null,
    pathZones: List<Zone> = emptyList(),
    onTransformGestures: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit = { _, _, _, _ -> },
    onTapGesture: (offset: Offset) -> Unit = {},
    onDoubleTapGesture: (offset: Offset) -> Unit = {},
    onHoldGesture: (offset: Offset) -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val poiPainters = rememberPoiPainters()
    val billboardColor = MaterialTheme.colorScheme.primary

    val (offset, scale, rotation, tilt) = cameraState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotate ->
                    onTransformGestures(centroid, pan, zoom, rotate)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset -> onTapGesture(offset) },
                    onDoubleTap = { offset -> onDoubleTapGesture(offset) },
                    onLongPress = { offset -> onHoldGesture(offset) }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val center = Offset(size.width / 2.0f, size.height / 2.0f)

            withTransform(
                transformBlock = {
                    translate(left = center.x + offset.x, top = center.y + offset.y)
                    scale(scaleX = scale, scaleY = scale * tilt, pivot = Offset.Zero)
                    rotate(degrees = rotation, pivot = Offset.Zero)
                }
            ) {
                if (floor != null) {
                    drawFloorPlan(floor, textMeasurer, currentZone, selectedZone, pathZones)
                }
            }


            floor?.pointsOfInterest?.forEach { poi ->
                val color = PoiTheme.get(poi.type).color
                val iconPainter = poiPainters.getPainter(poi.type)

                val (x, y) = (poi.x to poi.y)
                val (pX, pY) = calculateTransformedPosition(Offset(x, y), center, offset, scale, rotation, tilt)
                withTransform({
                    translate(left = pX, top = pY)
                }) {
                    val localPoi = poi.copy(x = 0f, y = 0f)
                    drawPoiPin(localPoi, textMeasurer, iconPainter, color)
                }
            }

            currentZone?.centerPos?.let { pos ->
                val (x, y) = pos
                val (pX, pY) = calculateTransformedPosition(Offset(x, y), center, offset, scale, rotation, tilt)
                withTransform({
                    translate(left = pX, top = pY)
                }) {
                    drawUserLocationBillboard(
                        textMeasurer = textMeasurer,
                        billboardColor = billboardColor
                    )
                }
            }
        }
    }
}

private fun calculateTransformedPosition(
    point: Offset,
    center: Offset,
    offset: Offset,
    scale: Float,
    rotation: Float,
    tilt: Float
): Offset {
    val rad = rotation * (kotlin.math.PI / 180.0)
    val cos = cos(rad).toFloat()
    val sin = sin(rad).toFloat()

    val rotatedX = point.x * cos - point.y * sin
    val rotatedY = point.x * sin + point.y * cos

    val finalX = center.x + offset.x + (rotatedX * scale)
    val finalY = center.y + offset.y + (rotatedY * scale * tilt)

    return Offset(finalX, finalY)
}