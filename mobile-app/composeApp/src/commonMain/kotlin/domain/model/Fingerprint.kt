package domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Fingerprint(
    val id: Uuid,
    val locationId: Uuid,
    val signals: List<BleTagSignal>
)