package data.filesystemProviders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePicker(onFilePicked: (File?) -> Unit): FilePicker {
    val context = LocalContext.current
    val picker = remember { AndroidFilePicker(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = picker.readFileFromUri(it)
            onFilePicked(file)
        } ?: onFilePicked(null)
    }

    DisposableEffect(picker, launcher) {
        picker.launcher = { launcher.launch(it) }
        onDispose {
            picker.launcher = null
        }
    }

    return picker
}