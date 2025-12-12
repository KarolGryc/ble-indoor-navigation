package presentation.buildingNavigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import domain.model.Wall

object WallStyles {
    val wallColor = Color.Black
    val wallThickness = 2f
}

fun DrawScope.drawWall(
    wall: Wall,
    color: Color = WallStyles.wallColor,
    thickness: Float = WallStyles.wallThickness
) {
    drawLine(
        color = color,
        start = Offset(wall.start.x, wall.start.y),
        end = Offset(wall.end.x, wall.end.y),
        strokeWidth = thickness
    )
}