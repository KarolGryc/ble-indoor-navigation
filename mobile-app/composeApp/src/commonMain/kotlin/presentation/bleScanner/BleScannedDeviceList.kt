package presentation.bleScanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.BleDevice
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BleDevicesList(
    devices: List<ScannedDeviceUi>,
    modifier: Modifier = Modifier
) {
    if (devices.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scanning...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "Detected: ${devices.size} device ${if(devices.size > 1) "s" else ""}",
                )
            }

            items(
                items = devices,
                key = { it.device.platformAddress}
            ) { scannedDevice ->
                val timeAgoText = remember(scannedDevice.lastSeen) {
                    val secondsAgo = scannedDevice.lastSeen / 1000
                    when {
                        secondsAgo < 60 -> "$secondsAgo s ago"
                        secondsAgo < 3600 -> "${secondsAgo / 60} min ago"
                        else -> "${secondsAgo / 3600} h ago"
                    }
                }

                BleDevicesListItem(
                    name = scannedDevice.device.name ?: "Unknown Device",
                    address = scannedDevice.device.platformAddress,
                    rssi = scannedDevice.device.rssi,
                    lastSeen = timeAgoText,
                    tagId = scannedDevice.device.tagId
                )
            }
        }
    }
}

@Preview
@Composable
fun BleDevicesListPreview() {
    BleDevicesList(
        devices = listOf(
            ScannedDeviceUi(
                device = BleDevice(
                    platformAddress = "00:11:22:33:44:55",
                    name = "Device 1",
                    rssi = -55,
                    tagId = 1
                ),
                lastSeen = 5000
            ),
            ScannedDeviceUi(
                device = BleDevice(
                    platformAddress = "66:77:88:99:AA:BB",
                    name = "Device 2",
                    rssi = -75,
                    tagId = null
                ),
                lastSeen = 15000
            ),
            ScannedDeviceUi(
                device = BleDevice(
                    platformAddress = "CC:DD:EE:FF:00:11",
                    name = "",
                    rssi = -85,
                    tagId = 42
                ),
                lastSeen = 30000
            )
        )
    )
    BleDevicesList(
        devices = emptyList()
    )
}