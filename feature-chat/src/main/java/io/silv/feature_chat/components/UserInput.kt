package io.silv.imagekeyboardtest.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                attachmentPickerSelected = attachmentPickerSelected,
                onTextChanged = { onTextChanged(it) } ,
                sendChat = { sendChat(text) },
                attachments = attachments,
                attachmentIconClicked = {
                    attachmentPickerSelected = true
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onDeleteAttachment = onDeleteAttachment
            )
        }
}

@Composable
fun ChatBar(
    text: String,
    attachmentPickerSelected: Boolean,
    attachments: List<Uri>,
    onTextChanged: (String) -> Unit,
    attachmentIconClicked: () -> Unit,
    sendChat: (String) -> Unit,
    onDeleteAttachment: (Uri) -> Unit,
) {

    var textFieldFocused by remember {
        mutableStateOf(false)
    }
    var focusRequester = remember { FocusRequester() }

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
        BasicTextField(
            value = text,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .shadow(2.dp, shape)
                .onFocusChanged {
                    textFieldFocused = it.hasFocus || it.isFocused
                },
            textStyle = TextStyle(fontSize = 18.sp),
            onValueChange = onTextChanged,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (textFieldFocused) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            shape
                        )
                        .padding(12.dp),
                    Alignment.CenterStart
                ) {
                    if (text.isEmpty() && !textFieldFocused) {
                        Text("send a message")
                    }
                    innerTextField()
                }
            }
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
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "attachment",
                    modifier = Modifier.size(80.dp).align(Alignment.Center)
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