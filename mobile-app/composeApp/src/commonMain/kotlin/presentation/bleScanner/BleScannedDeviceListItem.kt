package presentation.bleScanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import presentation.theme.NaviTheme

@Composable
fun BleDevicesListItem(
    name: String,
    address: String,
    rssi: Int,
    lastSeen: String,
    tagId: Int?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth Device",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name.ifBlank { "Unknown Device" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    tagId?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        TagBadge(tagId = it)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                RssiText(rssi)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastSeen,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TagBadge(tagId: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "Tag ID: $tagId",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RssiText(rssi: Int) {
    Text(
        text = "$rssi dBm",
        style = MaterialTheme.typography.labelLarge,
        color = getRssiColor(rssi),
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun getRssiColor(rssi: Int): Color {
    return when {
        rssi > -60 -> Color(0xFF2E7D32)
        rssi > -80 -> Color(0xFFF57F17)
        else -> Color(0xFFC62828)
    }
}

@Preview
@Composable
private fun BleDevicesListItemPreview() {
    NaviTheme {
        BleDevicesListItem(
            name = "Device Name",
            address = "00:11:22:33:44:55",
            rssi = -65,
            lastSeen = "10s ago",
            tagId = 1
        )
    }
}