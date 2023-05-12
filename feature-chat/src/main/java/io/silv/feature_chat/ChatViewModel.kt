package io.silv.feature_chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.datastore.EncryptedDatastore
import io.silv.feature_chat.types.Chat
import io.silv.feature_chat.types.MyChat
import io.silv.feature_chat.types.UiChat
import io.silv.feature_chat.types.UiUserInfo
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.DeleteAttachmentUseCase
import io.silv.feature_chat.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_chat.use_case.SendChatUseCase
import io.silv.feature_chat.use_case.WriteToAttachmentsUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val collectChatUseCase: CollectChatUseCase,
    private val sendChatUseCase: SendChatUseCase,
    private val writeToAttachmentsUseCase: WriteToAttachmentsUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase,
    private val datastore: EncryptedDatastore,
): EventViewModel<ChatEvent>() {

    private val mutableChatFlow = MutableStateFlow(emptyList<Chat>())
    private val serverConnected = MutableStateFlow<Boolean?>(null)
    private val imageAttachments = MutableStateFlow(emptyList<Uri>())
    private val users = MutableStateFlow<Map<String, UiUserInfo>>(emptyMap())
    private val message = MutableStateFlow(savedStateHandle["message"] ?: "")


    val chatUiState = combine(
        mutableChatFlow,
        serverConnected,
        imageAttachments,
        users,
        message
    ) { chats, serverConnected, attachments, users, message ->
        when (serverConnected) {
            null -> ChatUiState.Loading
            true -> ChatUiState.Success(
                chats = chats,
                imageAttachments = attachments,
                users = users,
                message = message
            )
            false -> ChatUiState.Error
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChatUiState.Loading)

    init {
        viewModelScope.launch {
            launch {
                datastore.readProfilePictureUri().first()?.let {
                    users.emit(
                        buildMap {
                            users.value.forEach { (k, v) ->
                                this[k] = v
                            }
                            this["me"] = UiUserInfo("me", it, "me")
                        }
                    )
                }
            }
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

    fun handleMessageChanged(text: String) = viewModelScope.launch {
        message.emit(text)
        savedStateHandle["message"] = text
    }

    fun onReceivedContent(uri: Uri) = viewModelScope.launch {
        val localUri = writeToAttachmentsUseCase(uri)
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
                when (data) {
                    is UiChat -> mutableChatFlow.getAndUpdate { list ->
                        list + data
                    }
                    is MyChat -> {
                        mutableChatFlow.getAndUpdate { it + data }
                    }
                    is UiUserInfo -> {
                        if (!users.value.contains(data.id)) {
                            users.getAndUpdate { users ->
                                buildMap {
                                    users.forEach { (k, v) ->
                                        this[k] = v
                                    }
                                    this[data.id] = data
                                }
                            }
                        }
                    }
                }
            }
        }.onLeft {
            eventChannel.send(
                ChatEvent.Error(it.message ?: "Unknown error")
            )
        }
    }

    fun deleteAttachment(uri: Uri) = viewModelScope.launch {
        imageAttachments.getAndUpdate { list ->
            list.filter { it != uri  }
        }
        deleteAttachmentUseCase(uri)
    }

    fun sendChat(message: String) = viewModelScope.launch {
        val chat = sendChatUseCase(message, imageAttachments.value)
        handleMessageChanged("")
        imageAttachments.emit(emptyList())
        mutableChatFlow.getAndUpdate { list ->
            list + chat
        }
    }
}

sealed class ChatUiState {
    object Loading: ChatUiState()
    data class Success(
        val chats: List<Chat> = emptyList(),
        val imageAttachments: List<Uri> = emptyList(),
        val users: Map<String, UiUserInfo>,
        val message: String
    ): ChatUiState()

    object Error: ChatUiState()
}

sealed class ChatEvent {
    object NavigateToHome: ChatEvent()

    object LostConnectionToGroup: ChatEvent()

    data class Error(val message: String): ChatEvent()
}