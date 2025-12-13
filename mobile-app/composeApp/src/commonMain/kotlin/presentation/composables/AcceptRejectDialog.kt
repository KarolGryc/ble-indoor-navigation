package presentation.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptRejectDialog(
    show: Boolean = false,
    title: String = "",
    message: String = "",
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val options = mutableListOf<DialogOption>()
    onAccept?.let { options.add(DialogOption("Accept") { onAccept() }) }
    onReject?.let { options.add(DialogOption("Reject") { onReject() }) }

    if (show) {
        DialogWithOptions(
            title = title,
            message = message,
            options = options,
            onDismiss = onReject
        )
    }
}

@Preview
@Composable
fun AcceptRejectDialogPreview() {
    AcceptRejectDialog(
        show = true,
        title = "Accept or reject?",
        message = "Please choose an option.",
        {},
        {}
    )
}