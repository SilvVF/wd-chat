package io.silv.feature_chat.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.feature_chat.types.Chat
import io.silv.feature_chat.types.MyChat
import io.silv.feature_chat.types.UiChat
import io.silv.shared_ui.components.RichContentEditText

@Composable
fun MessageView(
    modifier: Modifier = Modifier,
    chats: List<Chat>,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        items(chats.reversed()) { chat ->
            Spacer(modifier = Modifier.height(8.dp))
            when (chat) {
                is UiChat -> {
                    ReceivedChat(chat = chat)
                }
                is MyChat -> {
                    SentChat(chat = chat)
                }
            }
        }
    }
}

@Composable
fun SentChat(chat: MyChat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.End
    ) {
        LazyRow(Modifier.fillMaxWidth()) {
            items(chat.images) { image ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.padding(6.dp)

                )
            }
        }
        Text(text = chat.message)
    }
}

@Composable
fun ReceivedChat(chat: UiChat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.Start
    ) {
        LazyRow(Modifier.fillMaxWidth()) {
            items(chat.images) { image ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.padding(6.dp)

                )
            }
        }
        Text(text = chat.message)
    }
}
