package io.silv.feature_chat.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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

@SuppressLint("NewApi")
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
        val atApiR by remember {
            derivedStateOf {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            }
        }

        if (atApiR) {
            TextFieldApiUnderR(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .shadow(2.dp, shape)
                    .onFocusChanged {
                        textFieldFocused = it.isFocused
                    },
                text = text,
                shape = shape,
                onTextChanged = onTextChanged,
                focused = textFieldFocused
            )
        } else {
            TextFieldApiAtR(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .shadow(2.dp, shape)
                ,
                text = text,
                onTextChanged = onTextChanged
            )
        }

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

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TextFieldApiAtR(
    modifier: Modifier,
    text: String,
    onTextChanged: (String) -> Unit
) {
    fun insetsCallback(view: View) = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {

        var startBottom = 0f
        var endBottom = 0f

        override fun onPrepare(animation: WindowInsetsAnimation) {
            startBottom = view.bottom.toFloat()
        }

        override fun onStart(
            animation: WindowInsetsAnimation,
            bounds: WindowInsetsAnimation.Bounds
        ): WindowInsetsAnimation.Bounds {
            endBottom = view.bottom.toFloat()
            view.translationY = startBottom - endBottom
            return bounds
        }

        fun lerp(start: Float, stop: Float, fraction: Float): Float {
            return start * (1.0f - fraction) + (stop * fraction);
        }

        override fun onProgress(
            insets: WindowInsets,
            runningAnimations: MutableList<WindowInsetsAnimation>
        ): WindowInsets {
            val offset = lerp(
                start = startBottom,
                stop = endBottom,
                fraction = runningAnimations.first().interpolatedFraction
            )
            // ...which we then set using translationY
            view.translationY = offset

            return insets
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                EditText(it).apply {
                    setBackgroundColor(Color.Transparent.toArgb())
                    setText(text)
                    setWindowInsetsAnimationCallback(insetsCallback(this))
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

@Composable
fun TextFieldApiUnderR(
    modifier: Modifier,
    text: String,
    focused: Boolean,
    shape: RoundedCornerShape,
    onTextChanged: (String) -> Unit
) {
    BasicTextField(
        value = text,
        modifier = modifier,
        textStyle = TextStyle(fontSize = 18.sp),
        onValueChange = onTextChanged,
        decorationBox = { innerTextField ->
            Box(
                modifier = modifier,
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty() && !focused) {
                    Text("send a message")
                }
                innerTextField()
            }
        }
    )
}