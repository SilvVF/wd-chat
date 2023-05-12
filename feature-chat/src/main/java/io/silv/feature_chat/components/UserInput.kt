import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.feature_chat.ChatUiState
import io.silv.shared_ui.components.RichContentEditText

@Composable
fun UserInput(
    modifier: Modifier = Modifier,
    uiState: ChatUiState.Success,
    sendMessageEnabled: Boolean,
    onMessageChange: (String) -> Unit,
    onMessageSent: (String) -> Unit,
    onReceivedContent: (Uri) -> Unit,
    deleteAttachment: (Uri) -> Unit
) {

    Column(
        modifier = modifier
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                if (uiState.imageAttachments.isEmpty()) {
                    Text(text = "image attachments", color = Color.LightGray)
                }
            }
            items(uiState.imageAttachments) { uri ->
                Box(
                   modifier = Modifier
                       .padding(8.dp)
                       .clip(RoundedCornerShape(12.dp))
                       .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "attachment",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp)
                            .align(Alignment.Center)
                    )
                    IconButton(onClick = { deleteAttachment(uri) }, Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "delete",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.75f)
                    .padding(start = 12.dp, end = 6.dp)
            ) {
                    RichContentEditText(
                        text = uiState.message,
                        onTextChange = { onMessageChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        onReceivedContent = onReceivedContent,
                        deleteAttachment = deleteAttachment,
                        editTextBlock = {
                            this.setBackgroundColor(Color.Transparent.toArgb())
                            hint = "send a message"
                        }
                    )
            }
            val disabledContentColor =
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

            val buttonColors = ButtonDefaults.buttonColors(
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor
            )
            val border = if (!sendMessageEnabled) {
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            } else { null }
            // Send button
            Button(
                modifier = Modifier.height(36.dp),
                enabled = sendMessageEnabled,
                onClick = { onMessageSent(uiState.message) },
                colors = buttonColors,
                border = border,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "send",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}