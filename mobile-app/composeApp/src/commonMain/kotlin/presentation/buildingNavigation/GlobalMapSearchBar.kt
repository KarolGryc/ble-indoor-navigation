package presentation.buildingNavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Floor
import domain.model.PointOfInterest
import domain.model.Zone
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface MapSearchResult {
    val id: Uuid
    val title: String
    val subtitle: String

    data class ZoneResult(
        val zone: Zone,
        val floor: Floor
    ) : MapSearchResult {
        override val id = zone.id
        override val title = zone.name
        override val subtitle = "Zone • ${floor.name}"
    }

    data class PoiResult(
        val poi: PointOfInterest,
        val floor: Floor
    ) : MapSearchResult {
        override val id = poi.id
        override val title = poi.name
        override val subtitle = "Place • ${poi.type.name} • ${floor.name}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    searchResults: List<MapSearchResult>,
    onItemSearched: (MapSearchResult) -> Unit,
    onItemNavigatedTo: (MapSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { onActiveChange(false) },
                expanded = active,
                onExpandedChange = onActiveChange,
                placeholder = { Text("Where would you like to go?") },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { onActiveChange(false) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults) { result ->
                val itemIcon = when (result) {
                    is MapSearchResult.ZoneResult -> Icons.Default.ViewCompactAlt
                    is MapSearchResult.PoiResult -> PoiTheme.styles[result.poi.type]?.icon
                        ?: Icons.Default.TravelExplore
                }

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = { Text(result.title) },
                    supportingContent = { Text(result.subtitle) },
                    leadingContent = {
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = {
                                    onActiveChange(false)
                                    onItemSearched(result)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TravelExplore,
                                    contentDescription = "Show ${result.title} on map"
                                )
                            }

                            IconButton(
                                onClick = {
                                    onActiveChange(false)
                                    onItemNavigatedTo(result)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Navigate to ${result.title}"
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}