package data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UuidSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Uuid) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Uuid = Uuid.parse(decoder.decodeString())
}

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class BuildingMapDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val floors: List<FloorDto>,
    @SerialName("zone_connections")
    val zoneConnections: List<ZoneConnectionDto>
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class FloorDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    val nodes: List<NodeDto>,
    val walls: List<WallDto>,
    val zones: List<ZoneDto>,
    @SerialName("points_of_interest")
    val pointsOfInterest: List<PointOfInterestDto>
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class NodeDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val x: Float,
    val y: Float
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class WallDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @SerialName("start_node_id")
    @Serializable(with = UuidSerializer::class)
    val startNodeId: Uuid,
    @SerialName("end_node_id")
    @Serializable(with = UuidSerializer::class)
    val endNodeId: Uuid
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ZoneDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    val type: String,
    @SerialName("corner_node_ids")
    val cornerNodeIds: List<@Serializable(with = UuidSerializer::class) Uuid>
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PointOfInterestDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    val x: Float,
    val y: Float,
    val type: String
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ZoneConnectionDto(
    @SerialName("zone1_id")
    @Serializable(with = UuidSerializer::class)
    val zone1Id: Uuid,
    @SerialName("zone2_id")
    @Serializable(with = UuidSerializer::class)
    val zone2Id: Uuid
)