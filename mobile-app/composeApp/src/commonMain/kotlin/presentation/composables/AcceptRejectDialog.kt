package presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptRejectDialog(
    show: Boolean = false,
    titleText: String = "",
    dialogText: String = "",
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    if (show) {
        BasicAlertDialog(
            onDismissRequest = { onReject?.invoke() }
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 300.dp).wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = dialogText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (onReject != null) {
                            TextButton(onClick = onReject) { Text("Reject") }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (onAccept != null) {
                            TextButton(onClick = onAccept) { Text("Accept") }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AcceptRejectDialogPreview() {
    AcceptRejectDialog(
        show = true,
        titleText = "Wanna beer?",
        dialogText = "A cold one!",
        {},
        {}
    )
}