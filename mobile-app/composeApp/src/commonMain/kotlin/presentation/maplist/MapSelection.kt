package presentation.maplist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.filesystemProviders.rememberFilePicker
import domain.repository.MapInfo
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import presentation.theme.NaviTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MapAction(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapListScreen(
    viewModel: MapListViewModel = koinViewModel(),
    onNavigateToMap: (Uuid) -> Unit = { _ -> }
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scope) {
        viewModel.loadMapList()
    }
    val picker = rememberFilePicker { file ->
        file?.let {
            try {
                val fileName = it.name.replace(".json", "")
                viewModel.addMap(fileName, it.content)
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Available Maps") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { picker.pickFile() }) {
                Icon(Icons.Default.Add, contentDescription = "Add map")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MapList(
                maps = uiState.mapData,
                onMapSelected = onNavigateToMap,
                actions = listOf(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MapListItem(
    name: String,
    onClick: () -> Unit,
    actions: List<MapAction> = emptyList(),
    modifier: Modifier = Modifier
) {
    var isMenuExpended by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .defaultMinSize(minHeight = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (actions.isNotEmpty()) {
                Box {
                    IconButton(onClick = { isMenuExpended = true}) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More actions"
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpended,
                        onDismissRequest = { isMenuExpended = false }
                    ) {
                        actions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.label) },
                                leadingIcon = {
                                    action.icon?.let {
                                        Icon(imageVector = it, contentDescription = action.label)
                                    }
                                },
                                onClick = {
                                    isMenuExpended = false
                                    action.onClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MapListItemPreview() {
    NaviTheme {
        MapListItem(name = "Map", onClick = {}, actions = listOf(MapAction("Delete"){}))
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun MapList(
    maps: List<MapInfo>,
    onMapSelected: (Uuid) -> Unit,
    actions: List<MapAction> = emptyList(),
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(maps.size) { index ->
            MapListItem(
                name = maps[index].name,
                onClick = { onMapSelected(maps[index].id) },
                actions = actions,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun MapListPreview() {
    NaviTheme {
        MapList(
            maps = listOf(
                MapInfo("Map 1", Uuid.random()),
                MapInfo("Map 2", Uuid.random()),
                MapInfo("Map 3", Uuid.random())
            ),
            onMapSelected = {}
        )
    }
}