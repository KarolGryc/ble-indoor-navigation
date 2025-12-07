package presentation.bleScanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.model.BleDevice
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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
        Text(
            text = "Detected: ${devices.size} device${if(devices.size != 1) "s" else ""}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = devices,
                key = { it.device.platformAddress}
            ) { scannedDevice ->
                BleDevicesListItem(
                    name = scannedDevice.device.name ?: "Unknown Device",
                    address = scannedDevice.device.platformAddress,
                    rssi = scannedDevice.device.rssi,
                    lastSeen = scannedDevice.seenAgo.toString() + " ms ago",
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
                seenAgo = 5000
            ),
            ScannedDeviceUi(
                device = BleDevice(
                    platformAddress = "66:77:88:99:AA:BB",
                    name = "Device 2",
                    rssi = -75,
                    tagId = null
                ),
                seenAgo = 15000
            ),
            ScannedDeviceUi(
                device = BleDevice(
                    platformAddress = "CC:DD:EE:FF:00:11",
                    name = "",
                    rssi = -85,
                    tagId = 42
                ),
                seenAgo = 30000
            )
        )
    )
    BleDevicesList(
        devices = emptyList()
    )
}