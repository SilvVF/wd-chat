package io.silv.feature_chat

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.GetGroupInfoUseCase
import io.silv.shared_ui.utils.EventViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    getGroupInfoUseCase: GetGroupInfoUseCase,
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val collectChatUseCase: CollectChatUseCase
): EventViewModel<ChatEvent>() {

    private val mutableChatFlow = MutableStateFlow(emptyList<String>())
    private val serverConnected = MutableStateFlow(false)

    val chatUiState = combine(
        mutableChatFlow,
        serverConnected
    ) { chats, serverConnected ->
        ChatUiState(
            connectedToServer = serverConnected,
            messages = chats
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChatUiState())

    fun startChatServer(isGroupOwner: Boolean, groupOwnerAddress: String) = viewModelScope.launch {
        connectToChatUseCase(isGroupOwner, groupOwnerAddress)
    }
    private fun startCollectingChat() = viewModelScope.launch {
        collectChatUseCase().fold(
            ifLeft = {
                it.printStackTrace()
            },
            ifRight = {
                it.collect { chat ->
                    mutableChatFlow.getAndUpdate { list ->
                        list + chat.toString()
                    }
                }
            }
        )
    }
}

data class ChatUiState (
    val connectedToServer: Boolean = false,
    val messages: List<String> = emptyList()
)

sealed class ChatEvent() {

}