package data.filesystemProviders

import androidx.compose.runtime.Composable

interface FilePicker {
//    @Composable
//    fun registerPicker(onFilePicked: (File?) -> Unit)

    fun pickFile()
}

@Composable
expect fun rememberFilePicker(onFilePicked: (File?) -> Unit): FilePicker