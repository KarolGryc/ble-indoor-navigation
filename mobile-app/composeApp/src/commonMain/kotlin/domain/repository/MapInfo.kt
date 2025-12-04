package domain.repository

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class MapInfo(
    val name: String,
    val id: Uuid
)