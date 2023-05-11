@file:OptIn(ExperimentalMaterial3Api::class)

package io.silv.feature_chat


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
                navigateBack = {}
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
    navigateBack: () -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {

                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        }
    ) {
        it.calculateBottomPadding()
    }
}