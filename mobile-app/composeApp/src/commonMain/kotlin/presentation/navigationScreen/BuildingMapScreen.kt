package presentation.navigationScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

object MapStyles {
    val backgroundColor = Color.LightGray
}

data class MapCameraState(
    val offset: Offset = Offset.Zero,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val tilt: Float = 0.2f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel
) {
    val MAX_ZOOM = 3f
    val DEFAULT_ZOOM = 1.5f
    val MIN_ZOOM = 0.4f

    val uiState by viewModel.uiState.collectAsState()

    val tilt = 0.6f
    var targetScale by remember { mutableFloatStateOf(DEFAULT_ZOOM) }
    var targetRotation by remember { mutableFloatStateOf(0f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }

    val smoothSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)

    val animatedScale by animateFloatAsState(targetValue = targetScale, animationSpec = smoothSpec)
    val animatedRotation by animateFloatAsState(targetValue = targetRotation, animationSpec = smoothSpec)
    val animatedOffset by animateOffsetAsState(targetValue = targetOffset, animationSpec = spring(stiffness = Spring.StiffnessMedium))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Maps Menu") })
        },
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (targetScale < MAX_ZOOM) {
                            targetScale *= 1.5f
                            targetScale = targetScale.coerceIn(MIN_ZOOM, MAX_ZOOM)
                        } else {
                            targetScale = DEFAULT_ZOOM
                        }
                    },
                    onLongPress = {
                        targetRotation = 0f
                        targetScale = DEFAULT_ZOOM
                        targetOffset = Offset.Zero
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, twist ->
                    targetScale *= zoom
                    targetScale = targetScale.coerceIn(MIN_ZOOM, MAX_ZOOM)
                    targetRotation += twist
                    targetOffset += pan
                }
            }
        ) {
            if (uiState.isLoadingMap) {
                CircularProgressIndicator()
            }

            uiState.map?.let { map ->
                var selectedFloor by remember { mutableStateOf(map.floors.firstOrNull()) }
                selectedFloor?.let { floor ->
                    FloorMap(
                        floor = floor,
                        cameraState = MapCameraState(
                        offset = animatedOffset,
                        scale = animatedScale,
                        rotation = animatedRotation,
                        tilt = tilt
                        )
                    )
                }
            }
        }
    }
}