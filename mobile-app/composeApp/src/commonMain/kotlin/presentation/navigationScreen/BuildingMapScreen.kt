package presentation.navigationScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import presentation.navigationScreen.ViewportState.Companion.MAX_ZOOM

object MapStyles {
    val backgroundColor = Color.LightGray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFloor = uiState.selectedFloor

    val viewportState by viewModel.viewportState.collectAsState()
    val (targetOffset, targetScale, targetRotation, targetTilt) = viewportState


    val smoothSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val animatedScale by animateFloatAsState(targetValue = targetScale, animationSpec = smoothSpec)
    val animatedRotation by animateFloatAsState(targetValue = targetRotation, animationSpec = smoothSpec)
    val animatedOffset by animateOffsetAsState(targetValue = targetOffset, animationSpec = spring(stiffness = Spring.StiffnessMedium))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(selectedFloor?.name ?: "Unnamed") })
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(onClick = { viewModel.resetCamera() }) {
                    Text("Reset")
                }
                FloatingActionButton(onClick = { viewModel.changeFloorUp() }) {
                    Text("Up")
                }
                FloatingActionButton(onClick = { viewModel.changeFloorDown() }) {
                    Text("Down")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (targetScale < MAX_ZOOM) {
                            viewModel.updateViewport(scale = viewportState.scale * 1.5f)
                        } else {
                            viewModel.resetZoom()
                        }
                    },
                    onLongPress = { viewModel.resetCamera() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, twist ->
                    viewModel.updateViewport(
                        offset = viewportState.offset + pan,
                        scale = viewportState.scale * zoom,
                        rotation = viewportState.rotation + twist,
                    )
                }
            }
        ) {
            if (uiState.isLoadingMap) {
                CircularProgressIndicator()
            }

            selectedFloor?.let {
                FloorMap(
                    floor = it,
                    cameraState = ViewportState(
                        offset = animatedOffset,
                        scale = animatedScale,
                        rotation = animatedRotation,
                        tilt = targetTilt
                    )
                )
            }
        }
    }
}