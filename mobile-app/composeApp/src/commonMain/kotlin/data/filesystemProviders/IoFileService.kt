package data.filesystemProviders

interface IoFileService {
    suspend fun writeFile(directory: String, fileName: String, content: String)
    suspend fun readFile(directory: String, fileName: String): String
    suspend fun deleteFile(directory: String, fileName: String)
    fun exists(directory: String, fileName: String): Boolean
}