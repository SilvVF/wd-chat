@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.silv.feature_chat.components.ConfirmLeavePopup
import io.silv.feature_chat.components.ConversationContent

@Composable
fun ChatScreen(
  isGroupOwner: Boolean,
  groupOwnerAddress: String,
  viewModel: ChatViewModel = hiltViewModel(),
  navigateBack: () -> Unit
) {
    var confirmLeavePopupVisible by remember {
        mutableStateOf(false)
    }

    BackHandler {
        confirmLeavePopupVisible = true
    }

    LaunchedEffect(key1 = true) {
        viewModel.startChatServer(isGroupOwner, groupOwnerAddress)
    }

    val state = viewModel.chatUiState.collectAsStateWithLifecycle().value

    ConfirmLeavePopup(
        visible = confirmLeavePopupVisible,
        isGroupOwner = isGroupOwner,
        onDismiss = { confirmLeavePopupVisible = false },
        navigateBack = {
            viewModel.shutdownServer()
            navigateBack()
        }
    )

    when(state) {
        is ChatUiState.Success -> {
            ConversationContent(
                uiState = state,
                modifier = Modifier,
                onMessageSent = { viewModel.sendChat(it) },
                onMessageChange = { viewModel.handleMessageChanged(it) },
                onReceivedContent = { viewModel.onReceivedContent(it) },
                deleteAttachment = {viewModel.deleteAttachment(it)},
                navigateBack = {
                    confirmLeavePopupVisible = true
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


