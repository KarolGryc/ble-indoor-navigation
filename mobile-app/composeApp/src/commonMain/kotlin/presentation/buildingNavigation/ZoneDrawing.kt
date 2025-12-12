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
    PATH(Color(0xffffa46b))
}

fun DrawScope.drawZone(
    zone: Zone,
    state: ZoneState = ZoneState.NONE,
    opacity: Float = ZoneStyles.opacity,
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
    val cornerPoints = zone.boundary

    val minX = cornerPoints.minBy { it.x }.x
    val maxX = cornerPoints.maxBy { it.x }.x
    val minY = cornerPoints.minBy { it.y }.y
    val maxY = cornerPoints.maxBy { it.y }.y

    val centerX = (minX + maxX) / 2
    val centerY = (minY + maxY) / 2


    val nameEmote = when(zone.type) {
        ZoneType.STAIRS -> "\uD80C\uDE8D"
        ZoneType.ELEVATOR -> "\uD83D\uDED7"
        else -> ""
    }
    val baseStyle = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Medium, color = Color.Unspecified)
    val textLayoutResult = textMeasurer.measure(text = "${zone.name} $nameEmote", style = baseStyle)
    val textOffset = Offset(
        x = centerX - textLayoutResult.size.width / 2,
        y = centerY - textLayoutResult.size.height / 2
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