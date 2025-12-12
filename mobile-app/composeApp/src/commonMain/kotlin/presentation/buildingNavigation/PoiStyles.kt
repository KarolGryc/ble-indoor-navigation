package presentation.buildingNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import domain.model.PointOfInterestType

data class PoiStyle(
    val icon: ImageVector,
    val color: Color
)

object PoiTheme {
    val styles: Map<PointOfInterestType, PoiStyle> = mapOf(
        PointOfInterestType.GENERIC to PoiStyle(Icons.Default.Explore, Color(0xFFD41F1F)),
        PointOfInterestType.TOILET to PoiStyle(Icons.Default.Wc, Color(0xFF2166E5)),
        PointOfInterestType.SHOP to PoiStyle(Icons.Default.ShoppingBasket, Color(0xFFEE6A52)),
        PointOfInterestType.RESTAURANT to PoiStyle(Icons.Default.Restaurant, Color(0xFFF5B400)),
        PointOfInterestType.EXIT to PoiStyle(Icons.Default.DoorFront, Color(0xFF2A7F00))
    )

    fun get(type: PointOfInterestType): PoiStyle =
        styles[type] ?: styles[PointOfInterestType.GENERIC]!!
}