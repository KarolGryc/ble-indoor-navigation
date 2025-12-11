package data.service

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidFileSharer(
    private val context: Context
) : FileSharer {
    override suspend fun shareFile(file: data.filesystemProviders.File) : Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val savedFile = saveFile(file.name, file.content)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                savedFile
            )

            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags += Intent.FLAG_ACTIVITY_NEW_TASK
                    flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
                    type = "*/*"
                }

                val chooser = Intent.createChooser(intent, null)
                context.startActivity(chooser)
            }
        }
    }

    private fun saveFile(name: String, data: ByteArray): java.io.File {
        val cache = context.cacheDir
        val savedFile = java.io.File(cache, name)
        savedFile.writeBytes(data)
        return savedFile
    }
}

@Composable
actual fun rememberFileSharer(): FileSharer {
    val context = LocalContext.current
    return remember { AndroidFileSharer(context) }
}