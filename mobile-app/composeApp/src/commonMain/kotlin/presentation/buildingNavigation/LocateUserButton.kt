package presentation.buildingNavigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LocateUserButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    active: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = modifier.size(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            val activeButtonColor = Color(0.016f, 0.314f, 0.824f, 1.0f)
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Locate User",
                modifier = Modifier.size(24.dp),
                tint = if(active) activeButtonColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
fun LocateUserButtonPreview() {
    LocateUserButton(
        onClick = {}
    )
}

@Preview
@Composable
fun ActiveLocateUserButtonPreview() {
    LocateUserButton(
        onClick = {},
        active = true
    )
}