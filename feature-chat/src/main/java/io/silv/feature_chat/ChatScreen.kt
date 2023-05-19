@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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
                navigateBack()
            }
        )
        is ChatUiState.Loading -> ChatLoadingScreen()
    }
}

@Composable
fun ChatLoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_server_dots))
        val progress by animateLottieCompositionAsState(composition)

        LottieAnimation(composition = composition, progress = { progress })
    }
}
@Composable
fun ChatErrorScreen(
    retry: () -> Unit,
    navigateBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.error_connecting),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(
                onClick = { retry() }
            ) {
                Text(
                    text = stringResource(id = R.string.retry)
                )
            }
            OutlinedButton(
                onClick = { navigateBack() }
            ) {
                Text(
                    text = stringResource(id = R.string.navigate_back)
                )
            }
        }
    }
}


