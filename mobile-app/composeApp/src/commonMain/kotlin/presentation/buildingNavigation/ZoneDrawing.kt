package presentation.buildingNavigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import domain.model.Zone
import domain.model.ZoneType

object ZoneStyles {
    val opacity = 0.5f
    val fontSize = 20.sp
    val fontColor = Color.Black
    val fontBorderColor = Color.White
    val fontBorderThickness = 6f
}

enum class ZoneState(val color: Color) {
    NONE(Color(0xC9CBFFEE)),
    SELECTED(Color(0xffff0000)),
    CURRENT(Color(0xff2e75ff)),
    PATH(Color(0xBC6EFFF4)),
    PATH_GOAL(Color(0xff00ff00))
}

fun DrawScope.drawZone(
    zone: Zone,
    state: ZoneState = ZoneState.NONE,
    opacity: Float = ZoneStyles.opacity,
) {
    if (zone.boundary.isEmpty()) return

    val path = getZonePath(zone)

    val zoneColor = state.color
    drawPath(path = path, color = zoneColor, alpha = opacity)
}

fun DrawScope.drawZoneLabel(
    zone: Zone,
    textMeasurer: TextMeasurer,
    fontColor: Color = ZoneStyles.fontColor,
    fontBorderColor: Color = ZoneStyles.fontBorderColor,
    fontSize: TextUnit = ZoneStyles.fontSize,
    fontBorderThickness: Float = ZoneStyles.fontBorderThickness
) {
    val (posX, posY) = zone.centerPos

    val nameEmote = when(zone.type) {
        ZoneType.STAIRS -> "\uD80C\uDE8D"
        ZoneType.ELEVATOR -> "\uD83D\uDED7"
        else -> ""
    }
    val baseStyle = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Medium, color = Color.Unspecified)
    val textLayoutResult = textMeasurer.measure(text = "${zone.name} $nameEmote", style = baseStyle)
    val textOffset = Offset(
        x = posX - textLayoutResult.size.width / 2,
        y = posY - textLayoutResult.size.height / 2
    )

    drawText(
        textLayoutResult = textLayoutResult,
        color = fontBorderColor,
        topLeft = textOffset,
        drawStyle = Stroke(width = fontBorderThickness)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        color = fontColor,
        topLeft = textOffset,
        drawStyle = Fill
    )
}

private fun getZonePath(zone: Zone): Path {
    return Path().apply {
        val cornerPoints = zone.boundary
        val startPoint = cornerPoints.first()
        moveTo(startPoint.x, startPoint.y)

        for (i in 1..< cornerPoints.size) {
            val pos = cornerPoints[i]
            lineTo(pos.x, pos.y)
        }
        close()
    }
}