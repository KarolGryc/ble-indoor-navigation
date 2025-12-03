package data.filesystemProviders

import androidx.compose.runtime.Composable

interface FilePicker {
    fun pickFile()
}

@Composable
expect fun rememberFilePicker(onFilePicked: (File?) -> Unit): FilePicker