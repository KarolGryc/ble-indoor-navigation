package presentation.buildingNavigation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun DrawScope.drawUserLocationBillboard(
    textMeasurer: TextMeasurer,
    billboardColor: Color = Color(0xFFF7FFB7),
    textColor: Color = Color.White
) {
    val text = "You are here"

    val textStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
    )

    val textLayoutResult = textMeasurer.measure(text = text, style = textStyle)

    val padding = 8.dp.toPx()
    val arrowHeight = 12.dp.toPx()
    val arrowWidth = 16.dp.toPx()
    val cornerRadius = 8.dp.toPx()

    val boxWidth = textLayoutResult.size.width + (padding * 2)
    val boxHeight = textLayoutResult.size.height + (padding * 2)

    val boxLeft = -boxWidth / 2
    val boxTop = -boxHeight - arrowHeight

    val path = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(
                    offset = Offset(boxLeft, boxTop),
                    size = Size(boxWidth, boxHeight)
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        )

        moveTo(-arrowWidth / 2, boxTop + boxHeight)
        lineTo(0f, 0f)
        lineTo(arrowWidth / 2, boxTop + boxHeight)
        close()
    }

    drawPath(path = path, color = billboardColor, style = Fill)
    drawPath(path = path, color = Color.White, style = Stroke(width = 2.dp.toPx()))

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x = boxLeft + padding,
            y = boxTop + padding
        )
    )
}