package io.silv.feature_chat

import android.net.wifi.p2p.WifiP2pGroup
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.P2p
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    p2p: P2p
): EventViewModel<ChatEvent>() {

    var group by mutableStateOf<WifiP2pGroup?>(null)

    init {
        viewModelScope.launch {
            while (true) {
                p2p.requestGroupInfo().fold(
                    ifLeft = {
                        Log.d("Chat", it.message)
                    },
                    ifRight = {
                        group = it
                    }
                )
            }
        }
    }

}

sealed class ChatEvent() {

}