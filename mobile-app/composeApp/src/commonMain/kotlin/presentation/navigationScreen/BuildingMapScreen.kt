package presentation.navigationScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import presentation.composables.FloorSelectionPanel
import presentation.composables.MapCompass

object MapStyles {
    val backgroundColor = Color.LightGray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel,
    onClickBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFloor = uiState.selectedFloor

    val viewportState by viewModel.viewportState.collectAsState()
    val (targetOffset, targetScale, targetRotation, targetTilt) = viewportState

    val compasModeEnabled by viewModel.isCompassModeEnabled.collectAsState()

    val smoothSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val animatedScale by animateFloatAsState(targetValue = targetScale, animationSpec = smoothSpec)
    val animatedRotation by animateFloatAsState(targetValue = targetRotation, animationSpec = smoothSpec)
    val animatedOffset by animateOffsetAsState(targetValue = targetOffset, animationSpec = spring(stiffness = Spring.StiffnessMedium))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(selectedFloor?.name ?: "Unnamed") },
                navigationIcon = {
                    IconButton(onClick = onClickBack){
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription ="Back")
                    }
                },
                actions = {
                    IconButton(onClick={}){
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clipToBounds()
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
                    ),
                    onTransformGestures = { _, pan, zoom, rotation ->
                        if (rotation != 0f) {
                            viewModel.stopCompassMode()
                        }

                        viewModel.updateViewport(
                            offset = viewportState.offset + pan,
                            scale = viewportState.scale * zoom,
                            rotation = viewportState.rotation + rotation,
                        )
                    },
                    onHoldGesture = {
                        viewModel.resetCamera()
                        viewModel.stopCompassMode()
                    }
                )
            }

            MapCompass(
                rotation = animatedRotation,
                onClick = {
                    when {
                        compasModeEnabled -> {
                            viewModel.stopCompassMode()
                            viewModel.resetRotation()
                        }
                        viewportState.rotation == 0f -> viewModel.startCompassMode()
                        else -> viewModel.updateViewport(rotation = 0f)
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )

            FloorSelectionPanel(
                floorNum = uiState.floorNum,
                isMinFloor = uiState.isBottomLevel,
                isMaxFloor = uiState.isTopLevel,
                onFloorUp = { viewModel.changeFloorUp() },
                onFloorDown = { viewModel.changeFloorDown() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}