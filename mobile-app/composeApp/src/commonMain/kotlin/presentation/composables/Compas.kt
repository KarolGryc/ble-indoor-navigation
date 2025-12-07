package presentation.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview



@Composable
fun MapCompass(
    rotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = CircleShape)
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2

            val northPath = Path().apply {
                moveTo(cx, 0f)
                lineTo(cx + w * 0.3f, cy)
                lineTo(cx, cy - h * 0.1f)
                lineTo(cx - w * 0.3f, cy)
                close()
            }
            drawPath(northPath, color = Color.Red)

            val southPath = Path().apply {
                moveTo(cx, h)
                lineTo(cx + w * 0.3f, cy)
                lineTo(cx, cy - h * 0.1f)
                lineTo(cx - w * 0.3f, cy)
                close()
            }
            drawPath(southPath, color = Color.LightGray)
            drawCircle(Color.Black, radius = 2.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

@Preview
@Composable
fun PreviewCompass() {
    MapCompass(rotation = 45f, onClick = {})
}