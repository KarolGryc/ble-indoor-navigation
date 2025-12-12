package domain.extensions

import domain.model.Measurement

fun List<Measurement>.averageRssiByTag(): List<Measurement> {
    return this
        .groupBy { it.tagId }
        .map { (tagId, group) ->
            val avgRssi = group.map { it.rssi }.average().toInt()
            Measurement(tagId, avgRssi)
        }
}