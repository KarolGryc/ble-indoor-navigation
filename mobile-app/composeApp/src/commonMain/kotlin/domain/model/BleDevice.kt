package domain.model

data class BleDevice(
    val name: String?,
    val rssi: Int,
    val platformAddress: String,
    val tagId: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BleDevice

        if (rssi != other.rssi) return false
        if (name != other.name) return false
        if (platformAddress != other.platformAddress) return false
        if (!tagId.contentEquals(other.tagId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rssi
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + platformAddress.hashCode()
        result = 31 * result + (tagId?.contentHashCode() ?: 0)
        return result
    }
}