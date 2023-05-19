package io.silv.feature_chat.components

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun UserInput(
    modifier: Modifier = Modifier,
    text: String,
    onTextChanged: (String) -> Unit,
    attachments: List<Uri>,
    sendChat: (String) -> Unit,
    attachmentsReceived: (List<Uri>) -> Unit,
    onDeleteAttachment: (Uri) -> Unit
) {

    var attachmentPickerSelected by remember { mutableStateOf(false) }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> attachmentsReceived(uris) }
    )

    Column(
        modifier.drawWithContent {
            val h = this.size.height
            val w = this.size.width
            drawContent()
            drawLine(
                brush = Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)),
                start = Offset(0f, 0f),
                end = Offset(w, 0f)
            )
        }
    ) {
        AttachmentList(attachments = attachments, onDeleteAttachment = onDeleteAttachment)
        ChatBar(
                text = text,
                onTextChanged = { onTextChanged(it) } ,
                sendChat = { sendChat(text) },
                attachments = attachments,
                attachmentIconClicked = {
                    attachmentPickerSelected = true
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onDeleteAttachment = onDeleteAttachment,
                onReceivedContent = { attachmentsReceived(listOf(it)) }
            )
        }
}

@SuppressLint("NewApi")
@Composable
fun ChatBar(
    text: String,
    attachments: List<Uri>,
    onTextChanged: (String) -> Unit,
    attachmentIconClicked: () -> Unit,
    sendChat: (String) -> Unit,
    onReceivedContent: (Uri) -> Unit,
    onDeleteAttachment: (Uri) -> Unit,
) {

    var textFieldFocused by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(onClick = { attachmentIconClicked() }) {
            Icon(
                imageVector = Icons.Default.Attachment,
                contentDescription = "attachment",
                modifier = Modifier
                    .rotate(-60f)
                    .size(28.dp),
            )
        }
        val shape = RoundedCornerShape(32.dp)
        TextFieldStickerSupport(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                text = text,
                onFocusChanged = {
                    textFieldFocused = it
                },
                onTextChanged = onTextChanged,
                onReceivedContent = onReceivedContent
            )
        val enabled = text.isNotBlank() || attachments.isNotEmpty()

        IconButton(
            enabled = enabled,
            onClick = { sendChat(text) }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowCircleRight,
                contentDescription = "",
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AttachmentList(
    attachments: List<Uri>,
    onDeleteAttachment: (Uri) -> Unit
) {
    LazyRow {
        items(attachments, key = { it.path.toString() }) { uri ->
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(RoundedCornerShape(12.dp))
                .padding(4.dp)
                .clickable {
                    onDeleteAttachment(uri)
                }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "attachment",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                )
                IconButton(onClick = { onDeleteAttachment(uri) }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "delete attachment",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun TextFieldStickerSupport(
    modifier: Modifier,
    text: String,
    onReceivedContent: (Uri) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {


        val listener = remember {
            OnReceiveContentListener { _, payload ->

                val (content, rest) = payload.partition { item -> item.uri != null }

                content?.let {
                    for (i in 0 until it.clip.itemCount) {
                        val item = it.clip.getItemAt(i).uri
                        onReceivedContent(item)
                    }
                }
                rest
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                EditText(it).apply {
                    ViewCompat.setOnReceiveContentListener(
                        this, arrayOf("image/*", "video/*"), listener
                    )
                    setBackgroundColor(Color.Transparent.toArgb())
                    setText(text)
                    onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                        onFocusChanged(hasFocus)
                    }
                    doOnTextChanged { text, start, before, count ->
                        onTextChanged(text.toString())
                    }
                    hint = "send a message"
                    setPadding(0, 0, 0, 16)
                }
            },
            update = {
                if (text != it.text.toString()) {
                    it.setText(text)
                }
            }
        )
    }
}