package data.service

import data.filesystemProviders.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FileImportHandler {
    private val _fileContent = MutableStateFlow<File?>(null)
    val fileContent = _fileContent.asStateFlow()

    fun handleNewFile(file: File) {
        _fileContent.value = file
    }

    fun clear() {
        _fileContent.value = null
    }
}