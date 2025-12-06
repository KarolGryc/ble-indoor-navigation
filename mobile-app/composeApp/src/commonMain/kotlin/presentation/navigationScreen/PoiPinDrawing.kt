package presentation.navigationScreen

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
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.model.PointOfInterest

object PoiPinStyles {
    val pinRadius = 16.dp
    val pinHeight = 40.dp
    val iconSize = 16.dp
}

fun DrawScope.drawPoiPin(
    poi: PointOfInterest,
    textMeasurer: TextMeasurer,
    iconPainter: VectorPainter,
    color: Color = Color.Red,
    borderColor: Color = Color.White,
    iconColor: Color = Color.White,
    textColor: Color = Color.Black,
    textStrokeColor: Color = Color.White,
    pinRadius: Float = PoiPinStyles.pinRadius.toPx(),
    pinHeight: Float = PoiPinStyles.pinHeight.toPx(),
    iconSize: Float = PoiPinStyles.iconSize.toPx()
) {
    val tip = Offset(poi.x, poi.y)
    val pinPath = createPinPath(tip, pinRadius, pinHeight)

    translate(left = 2f, top = 2f) {
        drawPath(path = pinPath, color = Color.Black.copy(alpha = 0.3f))
    }

    drawPath(path = pinPath, color = color)

    drawPath(
        path = pinPath,
        color = borderColor,
        style = Stroke(width = 3.dp.toPx())
    )

    val headCenterY = poi.y - pinHeight + pinRadius

    translate(left = poi.x - iconSize / 2, top = headCenterY - iconSize / 2) {
        with(iconPainter) {
            draw(
                size = Size(iconSize, iconSize),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }

    val textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Unspecified)

    val textLayoutResult = textMeasurer.measure(text = poi.name, style = textStyle)

    val margin = 8.dp.toPx()
    val textLeft = poi.x + pinRadius + margin
    val textTop = headCenterY - (textLayoutResult.size.height / 2)
    val textOffset = Offset(textLeft, textTop)

    drawText(
        textLayoutResult = textLayoutResult,
        color = textStrokeColor,
        topLeft = textOffset,
        drawStyle = Stroke(width = 3f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        color = textColor,
        topLeft = textOffset,
        drawStyle = Fill
    )
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