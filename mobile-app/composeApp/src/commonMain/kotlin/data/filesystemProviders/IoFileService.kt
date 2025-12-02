package data.filesystemProviders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

class IoFileService(
    private val pathProvider: StoragePathProvider
) {
    private val fileSystem = SystemFileSystem

    suspend fun saveFile(directory: String, fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            val path = getFullPath(directory, fileName)

            fileSystem.sink(path).buffered().use { sink ->
                sink.writeString(content)
            }
        }
    }

    suspend fun readFile(directory: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val path = getFullPath(directory, fileName)

            fileSystem.source(path).buffered().use { source ->
                source.readString()
            }
        }
    }

    suspend fun listFiles(directory: String, extension: String): List<String> {
        return withContext(Dispatchers.IO) {
            val dirPath = Path(pathProvider.getAppFilesPath(), directory)

            if (!fileSystem.exists(dirPath)) {
                return@withContext emptyList<String>()
            }

            fileSystem.list(dirPath)
                .filter { it.name.endsWith(extension) }
                .map { it.name }
        }
    }

    private fun getFullPath(directory: String, fileName: String): Path {
        return Path(pathProvider.getAppFilesPath(), directory, fileName)
    }
}