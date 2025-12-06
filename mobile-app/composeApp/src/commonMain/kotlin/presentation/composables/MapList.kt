package presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import domain.repository.MapInfo
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.theme.NaviTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class MapAction(
    val label: String = "",
    val icon: ImageVector? = null,
    val onClick: (MapInfo) -> Unit
)

/**
 * Single map item in a list.
 *
 * @param info Details about map .
 * @param onClick Callback invoked when the map item is clicked.
 * @param actions List of actions available for the map item.
 * @param modifier The modifier to be applied to the [MapListItem]
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun MapListItem(
    info: MapInfo,
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
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = info.name,
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
                                    action.onClick(info)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun MapListItemPreview() {
    NaviTheme {
        MapListItem(
            info = MapInfo("Sample Map", Uuid.random()),
            onClick = {},
            actions = listOf(MapAction("Delete"){})
        )
    }
}


/**
 * Displays a list of maps as [MapListItem].
 *
 * @param mapInfos The list of maps to display.
 * @param onMapSelected A callback invoked when a map is selected, providing the map's UUID.
 * @param actions A list of actions available for each map item.
 * @param modifier The modifier to be applied to the [MapListItem]
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun MapList(
    mapInfos: List<MapInfo>,
    onMapSelected: (Uuid) -> Unit,
    actions: List<MapAction> = emptyList(),
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(mapInfos) { mapInfo ->
            MapListItem(
                info = mapInfo,
                onClick = { onMapSelected(mapInfo.id) },
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
            mapInfos = listOf(
                MapInfo("Map 1", Uuid.random()),
                MapInfo("Map 2", Uuid.random()),
                MapInfo("Map 3", Uuid.random())
            ),
            onMapSelected = {}
        )
    }
}