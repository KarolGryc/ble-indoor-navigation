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
        PointOfInterest(id = Uuid.random(), name = "Generic", x = 200f, y = 200f, type = PointOfInterestType.GENERIC),
        PointOfInterest(id = Uuid.random(), name = "Toilet", x = -200f, y = -200f, type = PointOfInterestType.TOILET),
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
    floor: Floor,
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
                    translate(left = center.x, top = center.y)
                    scale(scaleX = scale, scaleY = scale * tilt, pivot = Offset.Zero)
                    rotate(degrees = rotation, pivot = Offset.Zero)
                    translate(left = offset.x, top = offset.y)
                }
            ) {
                drawFloorPlan(floor, textMeasurer, currentZone, selectedZone, pathZones)

                floor.pointsOfInterest.forEach { poi ->
                    val color = PoiTheme.get(poi.type).color
                    val iconPainter = poiPainters.getPainter(poi.type)

                    withTransform({
                        translate(left = poi.x, top = poi.y)
                        rotate(degrees = -rotation, pivot = Offset.Zero)
                        scale(scaleX = 1f / scale, scaleY = 1f / (scale * tilt), pivot = Offset.Zero)
                    }) {
                        drawPoiPin(poi.name, textMeasurer, iconPainter, color)
                    }
                }

                if (currentZone != null && floor.zones.contains(currentZone)) {
                    val (x, y) = currentZone.centerPos
                    withTransform({
                        translate(left = x, top = y)
                        rotate(degrees = -rotation, pivot = Offset.Zero)
                        scale(scaleX = 1f / scale, scaleY = 1f / (scale * tilt), pivot = Offset.Zero)
                    }) {
                        drawUserLocationBillboard(textMeasurer, billboardColor)
                    }
                }
            }
        }
    }
}