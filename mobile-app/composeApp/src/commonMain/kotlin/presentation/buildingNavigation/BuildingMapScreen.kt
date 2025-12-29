package presentation.buildingNavigation

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import presentation.composables.AcceptRejectDialog
import presentation.composables.FloorSelectionPanel
import presentation.composables.MapCompass
import presentation.composables.NavigationTargetCapsule
import presentation.permissions.PermissionCheckResult
import presentation.permissions.checkPermissions
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingMapScreen(
    viewModel: MapNavigationViewModel,
    onClickBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.uiErrorMessage
    val currentFloor = uiState.currentFloor
    val currentZone = uiState.currentZone

    val isSearching = uiState.isSearching
    val searchQuery = uiState.searchQuery

    val viewportState by viewModel.viewportState.collectAsState()
    val (targetOffset, targetScale, targetRotation, targetTilt) = viewportState

    val compasModeEnabled by viewModel.isCompassModeEnabled.collectAsState()

    val smoothSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val animatedScale by animateFloatAsState(targetValue = targetScale, animationSpec = smoothSpec)
    val animatedRotation by animateFloatAsState(targetValue = targetRotation, animationSpec = smoothSpec)
    val animatedOffset by animateOffsetAsState(targetValue = targetOffset, animationSpec = spring(stiffness = Spring.StiffnessMedium))

    Scaffold(
        topBar = {
            if (isSearching) {
                MapSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    active = isSearching,
                    onActiveChange = { viewModel.setIsSearching(it) },
                    searchResults = uiState.filteredSearchResults,
                    onItemSearched = { item -> viewModel.showSearchedItem(item) },
                    onItemNavigatedTo = { item ->
                        getPermissionsExecute(
                            viewModel = viewModel,
                            controller = controller,
                            scope = scope,
                            onSuccess = {
                                viewModel.startLocationTracking()
                                viewModel.startNavigation(item)
                            }
                        )
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text(currentFloor?.name ?: "Unnamed") },
                    navigationIcon = {
                        IconButton(onClick = onClickBack){
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription ="Back")
                        }
                    },
                    actions = {
                        IconButton(onClick={ viewModel.setIsSearching(true) }){
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
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

            currentFloor?.let {
                FloorMap(
                    floor = currentFloor,
                    currentZone = currentZone,
                    cameraState = ViewportState(
                        offset = animatedOffset,
                        scale = animatedScale,
                        rotation = animatedRotation,
                        tilt = targetTilt
                    ),
                    onTransformGestures = { _, pan, zoom, rotation ->
                        if (rotation != 0f) viewModel.stopCompassMode()

                        val currentState = viewportState

                        val angleRad = -currentState.rotation * (PI / 180f).toFloat()
                        val cosAngle = kotlin.math.cos(angleRad)
                        val sinAngle = kotlin.math.sin(angleRad)

                        val rotatedPanX = pan.x * cosAngle - pan.y * sinAngle
                        val rotatedPanY = pan.x * sinAngle + pan.y * cosAngle

                        val deltaX = rotatedPanX / currentState.scale
                        val deltaY = rotatedPanY / currentState.scale

                        viewModel.updateViewport(
                            offset = Offset(
                                x = currentState.offset.x + deltaX,
                                y = currentState.offset.y + deltaY
                            ),
                            scale = currentState.scale * zoom,
                            rotation = currentState.rotation + rotation
                        )
                    },
                    onHoldGesture = {
                        viewModel.resetCamera()
                        viewModel.stopCompassMode()
                    },
                    pathZones = uiState.currentPath ?: emptyList()
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
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )

            LocateUserButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                onClick = {
                    if (uiState.locationEnabled) {
                        viewModel.showCurrentZone()
                    }
                    else {
                        getPermissionsExecute(
                            viewModel = viewModel,
                            controller = controller,
                            scope = scope,
                            onSuccess = viewModel::startLocationTracking
                        )
                    }
                },
                active = uiState.locationEnabled
            )

            FloorSelectionPanel(
                floorNum = uiState.floorNum,
                isMinFloor = uiState.isBottomLevel,
                isMaxFloor = uiState.isTopLevel,
                onFloorUp = { viewModel.incrementFloor() },
                onFloorDown = { viewModel.decrementFloor() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )

            if (uiState.isNavigating) {
                NavigationTargetCapsule(
                    destinationName = uiState.nextNavigatedLocation?.name ?: "",
                    onCloseClick = { viewModel.stopNavigation() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .scale(1.5f)
                        .padding(16.dp)
                )
            }

            AcceptRejectDialog(
                show = errorMessage != null,
                title = errorMessage?.title ?: "",
                message = errorMessage?.message ?: "",
                onAccept = { viewModel.clearErrorMessage() }
            )
        }
    }
}

private fun getPermissionsExecute(
    viewModel: MapNavigationViewModel,
    controller: PermissionsController,
    scope: CoroutineScope,
    onSuccess: () -> Unit
) {
    scope.launch {
        val permissionResult = checkPermissions(
            permissions = listOf(Permission.LOCATION, Permission.BLUETOOTH_SCAN),
            controller = controller,
        )

        when (permissionResult) {
            PermissionCheckResult.Granted -> onSuccess()
            PermissionCheckResult.PermanentlyDenied ->
                viewModel.setErrorMessage(
                    "Permissions Denied",
                    "Location and Bluetooth permissions are permanently denied. Please enable them in settings."
                )
            else -> {}
        }
    }
}