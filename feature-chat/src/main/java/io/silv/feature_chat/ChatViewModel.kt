package io.silv.feature_chat

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.UserInfo
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_chat.use_case.SendChatUseCase
import io.silv.image_store.ImageRepository
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val collectChatUseCase: CollectChatUseCase,
    private val sendChatUseCase: SendChatUseCase,
    private val imageStore: ImageRepository
): EventViewModel<ChatEvent>() {

    private val mutableChatFlow = MutableStateFlow(emptyList<String>())
    private val serverConnected = MutableStateFlow<Boolean?>(null)
    private val imageAttachments = MutableStateFlow(emptyList<Uri>())
    private val otherUsers = MutableStateFlow<List<UserInfo>>(emptyList())

    val chatUiState = combine(
        mutableChatFlow,
        serverConnected,
        imageAttachments
    ) { chats, serverConnected, attachments ->
        when (serverConnected) {
            null -> ChatUiState.Loading
            true -> ChatUiState.Success(
                messages = chats,
                imageAttachments = attachments
            )
            false -> ChatUiState.Error
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChatUiState.Loading)
    init {
        viewModelScope.launch {
            observeWifiDirectEventsUseCase().collect { event ->
                when (event) {
                    is WifiP2pEvent.ConnectionChanged -> {
                        if (!event.p2pInfo.groupFormed) {
                            eventChannel.send(ChatEvent.LostConnectionToGroup)
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    fun onReceivedContent(uri: Uri) = viewModelScope.launch {
        val localUri = imageStore.write(uri)
        imageAttachments.getAndUpdate {
            it + localUri
        }
    }

    fun startChatServer(isGroupOwner: Boolean, groupOwnerAddress: String) = viewModelScope.launch {
        connectToChatUseCase(isGroupOwner, groupOwnerAddress).also { connected ->
            serverConnected.emit(connected)
            if (connected) {
                startCollectingChat()
            }
        }
    }
    private fun startCollectingChat() = viewModelScope.launch {
        collectChatUseCase().onRight { wsData ->
            wsData.collect { data ->
                mutableChatFlow.getAndUpdate { list ->
                    list + data.toString()
                }
            }
        }.onLeft {
            eventChannel.send(
                ChatEvent.Error(it.message ?: "Unknown error")
            )
        }
    }

    fun sendChat(message: String) = viewModelScope.launch {
        sendChatUseCase(message, imageAttachments.value)
    }
}

sealed class ChatUiState {
    object Loading: ChatUiState()
    data class Success(
        val messages: List<String> = emptyList(),
        val imageAttachments: List<Uri> = emptyList()
    ): ChatUiState()

    object Error: ChatUiState()
}

sealed class ChatEvent {
    object NavigateToHome: ChatEvent()

    object LostConnectionToGroup: ChatEvent()

    data class Error(val message: String): ChatEvent()
}