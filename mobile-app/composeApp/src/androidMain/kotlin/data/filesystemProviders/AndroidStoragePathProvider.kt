package data.filesystemProviders

import android.content.Context
import kotlinx.io.files.Path

class AndroidStoragePathProvider(
    private val context: Context
) : StoragePathProvider {
    override fun getAppFilesPath(): Path {
        return Path(context.filesDir.absolutePath)
    }
}