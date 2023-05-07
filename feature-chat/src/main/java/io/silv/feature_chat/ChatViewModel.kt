package io.silv.feature_chat

import android.net.wifi.p2p.WifiP2pGroup
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.WsObj
import io.silv.feature_chat.use_case.CollectChatUseCase
import io.silv.feature_chat.use_case.ConnectToChatUseCase
import io.silv.feature_chat.use_case.GetGroupInfoUseCase
import io.silv.feature_chat.use_case.collectChatUseCaseImpl
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.P2p
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    getGroupInfoUseCase: GetGroupInfoUseCase,
    connectToChatUseCase: ConnectToChatUseCase,
    private val collectChatUseCase: CollectChatUseCase
): EventViewModel<ChatEvent>() {

    var group by mutableStateOf<WifiP2pGroup?>(null)

    private val mutableChatFlow = MutableStateFlow(emptyList<WsObj>())
    val chat = mutableChatFlow.asStateFlow()

    init {
        viewModelScope.launch {
            getGroupInfoUseCase().fold(
                ifLeft = {
                    // handle unable to get info
                },
                ifRight = { groupFlow ->
                    groupFlow.collectIndexed { idx, group ->
                        if (idx == 0) {
                            connectToChatUseCase(group).run {
                                if (this) { startCollectingChat() }
                            }
                        }
                        // handle group changed
                    }
                }
            )
        }
    }


    private fun startCollectingChat() = viewModelScope.launch {
        collectChatUseCase().fold(
            ifLeft = {
                it.printStackTrace()
            },
            ifRight = {
                it.collect { chat ->
                    mutableChatFlow.getAndUpdate { list ->
                        list + chat
                    }
                }
            }
        )
    }

}

sealed class ChatEvent() {

}