package data.filesystemProviders

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

class AndroidFilePicker(
    private val context: Context
) : FilePicker {
    var launcher: ((String) -> Unit)? = null

    override fun pickFile() {
        launcher?.invoke("*/*")
    }

    internal fun readFileFromUri(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val name = getFileName(uri) ?: "Unknown"
            val content = contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: return null

            File(name, content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver
            .query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}