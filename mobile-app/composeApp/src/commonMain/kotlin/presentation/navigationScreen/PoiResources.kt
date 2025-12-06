package presentation.navigationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import domain.model.PointOfInterestType

class PoiPainters(
    private val painters: Map<PointOfInterestType, VectorPainter>
) {
    fun getPainter(type: PointOfInterestType): VectorPainter {
        return painters[type] ?: painters[PointOfInterestType.GENERIC]!!
    }
}

@Composable
fun rememberPoiPainters(): PoiPainters {
    val allStyles = PoiTheme.styles

    val painters = allStyles.mapValues { (_, style) ->
        rememberVectorPainter(style.icon)
    }

    return remember(painters) { PoiPainters(painters) }
}