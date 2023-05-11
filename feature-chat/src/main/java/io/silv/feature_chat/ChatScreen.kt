@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.silv.feature_chat.components.MessageView
import io.silv.shared_ui.components.RichContentEditText

@Composable
fun ChatScreen(
  isGroupOwner: Boolean,
  groupOwnerAddress: String,
  viewModel: ChatViewModel = hiltViewModel()
) {

    LaunchedEffect(key1 = true) {
        viewModel.startChatServer(isGroupOwner, groupOwnerAddress)
    }

    val state = viewModel.chatUiState.collectAsStateWithLifecycle().value

    when(state) {
        is ChatUiState.Success -> {
            ChatSuccessScreen(
                state = state,
                navigateBack = {

                },
                onReceivedContent = {
                    viewModel.onReceivedContent(it)
                },
                sendChat = {
                    viewModel.sendChat(it)
                },
                onMessageChanged = {
                    viewModel.handleMessageChanged(it)
                }
            )
        }
        is ChatUiState.Error -> ChatErrorScreen(
            retry = {
                viewModel.startChatServer(isGroupOwner, groupOwnerAddress)
            },
            navigateBack = {

            }
        )
        is ChatUiState.Loading -> ChatLoadingScreen()
    }
}

@Composable
fun ChatLoadingScreen() {
    Surface(Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}
@Composable
fun ChatErrorScreen(
    retry: () -> Unit,
    navigateBack: () -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        Text(text = "Error Occured")
        Button(onClick = { retry() }) {
            Text(text = "Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSuccessScreen(
    state: ChatUiState.Success,
    sendChat: (message: String) -> Unit,
    onReceivedContent: (uri: Uri) -> Unit,
    onMessageChanged: (String) -> Unit,
    navigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Row {
                       for (user in state.users) {
                           AsyncImage(
                               model = ImageRequest.Builder(LocalContext.current)
                                   .data(user.icon)
                                   .crossfade(true)
                                   .build(),
                               contentDescription = "",
                               contentScale = ContentScale.Crop,
                               modifier = Modifier
                                   .padding(6.dp)
                                   .clip(CircleShape)
                           )
                       }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding.calculateTopPadding())
        ) {
            MessageView(
                modifier = Modifier.weight(0.8f, true).fillMaxWidth(),
                chats = state.chats,
            )
            Row(Modifier.weight(0.2f, true)) {
                Column {
                    LazyRow {
                        items(state.imageAttachments) {uri ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    RichContentEditText(
                        text = state.message,
                        onTextChange = { onMessageChanged(it) },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onReceivedContent = onReceivedContent
                    )
                }
                IconButton(
                    onClick = {
                        sendChat(state.message)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "")
                }
            }
        }
    }
}

