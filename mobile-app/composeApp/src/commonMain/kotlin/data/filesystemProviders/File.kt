package data.filesystemProviders

data class File (
    val name: String,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || this::class != other::class)
            return false

        other as File
        return name == other.name && content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}