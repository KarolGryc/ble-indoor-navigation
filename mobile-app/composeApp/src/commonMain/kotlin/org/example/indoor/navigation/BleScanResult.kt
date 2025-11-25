package org.example.indoor.navigation

data class BleScanResult(
    val name: String?,
    val identifier: String,
    val manufacturerData: List<Pair<Int, ByteArray>>,
    val rssi: Int,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BleScanResult

        if (rssi != other.rssi) return false
        if (name != other.name) return false
        if (identifier != other.identifier) return false
        if (manufacturerData.size != other.manufacturerData.size ||
                manufacturerData.indices.any { i ->
                    manufacturerData[i].first != other.manufacturerData[i].first ||
                    !manufacturerData[i].second.contentEquals(other.manufacturerData[i].second)
            }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rssi
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + identifier.hashCode()
        result = 31 * result + (manufacturerData.deepHashCode())
        return result
    }

    private fun List<Pair<Int, ByteArray>>.deepHashCode(): Int {
        var result = 1
        for ((key, value) in this) {
            val keyHash = key.hashCode()
            val valueHash = value.contentHashCode()
            result = 31 * result + (31 * keyHash + valueHash)
        }
        return result
    }
}