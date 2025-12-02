package data.filesystemProviders

import kotlinx.io.files.Path

interface StoragePathProvider {
    fun getAppFilesPath(): Path
}