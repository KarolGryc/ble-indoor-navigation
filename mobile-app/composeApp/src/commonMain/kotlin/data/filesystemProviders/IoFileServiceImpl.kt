package data.filesystemProviders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

class IoFileServiceImpl(
    private val pathProvider: StoragePathProvider
) : IoFileService {
    private val fileSystem = SystemFileSystem

    override suspend fun writeFile(directory: String, fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            val path = getFullPath(directory, fileName)

            val parentDir = path.parent ?: throw IOException("Invalid path: $path")

            if (!fileSystem.exists(parentDir)) {
                fileSystem.createDirectories(parentDir)
            }

            fileSystem.sink(path).buffered().use { sink ->
                sink.writeString(content)
            }
        }
    }

    override suspend fun readFile(directory: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            val path = getFullPath(directory, fileName)

            fileSystem.source(path).buffered().use { source ->
                source.readString()
            }
        }
    }

    override fun exists(directory: String, fileName: String): Boolean {
        val path = getFullPath(directory, fileName)
        return fileSystem.exists(path)
    }

    private fun getFullPath(directory: String, fileName: String): Path {
        return Path(pathProvider.getAppFilesPath(), directory, fileName)
    }
}