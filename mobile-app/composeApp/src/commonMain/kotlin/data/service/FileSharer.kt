package data.service

import androidx.compose.runtime.Composable
import data.filesystemProviders.File

interface FileSharer {
    suspend fun shareFile(file: File): Result<Unit>
}

@Composable
expect fun rememberFileSharer(): FileSharer