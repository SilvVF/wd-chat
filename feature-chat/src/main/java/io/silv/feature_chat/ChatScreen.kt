@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat

import android.widget.ImageView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        is ChatUiState.Success -> ChatSuccessScreen(state = state, viewModel = viewModel)
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
    viewModel: ChatViewModel
) {
    var text by remember {
        mutableStateOf("")
    }
    Column() {

        RichContentEditText(
            text = text,
            onTextChange = {
                text = it
            },
            editTextBlock = {
                this.height = 200
                this.width = 500
            },
            onReceivedContent = { uri ->
                viewModel.onReceivedContent(uri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(state.imageAttachments) { uri ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            setImageURI(uri)
                        }
                    },
                    update = { view ->
                        view.setImageURI(uri)
                    }
                )
            }
            item {
                Button(onClick = {
                    viewModel.sendChat(text)
                    text = ""
                }) {
                    Text(text = "chat")
                }
            }
            items(state.messages) {
                Text(text = it)
            }
        }
    }
}