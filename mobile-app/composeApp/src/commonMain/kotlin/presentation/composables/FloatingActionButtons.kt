package presentation.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.theme.NaviTheme

data class GeneralAction(
    val label: String = "",
    val icon: ImageVector? = null,
    val onClick: () -> Unit
)

@Composable
fun FloatingActions(
    actions: List<GeneralAction> = emptyList(),
    isExpandedDefault: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(isExpandedDefault) }

    Column {
        AnimatedVisibility(isExpanded) {
            LazyColumn {
                items(actions) { action ->
                    FloatingActionButton(
                        onClick = {
                            isExpanded = false
                            action.onClick()
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        action.icon?.let {
                            Icon(it, contentDescription = action.label)
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded }
        ) {
            val icon = if (isExpanded) Icons.Default.MoveDown else Icons.Default.MoveUp
            Icon(icon, contentDescription = "Add map")
        }
    }
}

@Preview
@Composable
private fun FloatingButtonsPreview() {
    NaviTheme {
        FloatingActions(
            actions = listOf(
                GeneralAction("Add from file", Icons.Default.Add) {},
                GeneralAction("Add from camera", Icons.Default.QrCode) {}
            ),
            isExpandedDefault = true
        )
    }
}