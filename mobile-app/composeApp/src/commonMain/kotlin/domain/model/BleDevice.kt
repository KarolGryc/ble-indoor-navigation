package domain.model

data class BleDevice(
    val name: String?,
    val rssi: Int,
    val platformAddress: String,
    val tagId: Int?
)