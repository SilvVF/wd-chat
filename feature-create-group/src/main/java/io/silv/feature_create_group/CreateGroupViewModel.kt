package io.silv.feature_create_group

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_create_group.use_case.CreateGroupUseCase
import io.silv.feature_create_group.use_case.GroupInfo
import io.silv.feature_create_group.use_case.ObserveWifiDirectEventsUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
): EventViewModel<CreateGroupEvent>() {
    init {
        viewModelScope.launch {
            observeWifiDirectEventsUseCase().collect { event ->
                when(event) {
                    is WifiP2pEvent.ConnectionChanged -> {
                        if (event.p2pInfo.groupFormed) {
                            eventChannel.send(
                                CreateGroupEvent.GroupCreated(
                                    event.p2pInfo.isGroupOwner,
                                    event.p2pInfo.groupOwnerAddress
                                        .toString()
                                        .replace("/", "")
                                )
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
    fun createGroup() = viewModelScope.launch {
        createGroupUseCase(
            GroupInfo(
                networkName =  "TestNet",
                passPhrase = "password"
            )
        )
            .onLeft { error ->
                eventChannel.send(
                    CreateGroupEvent.ShowToast(error.message)
                )
            }
    }

}

sealed class CreateGroupEvent {
    data class GroupCreated(val isGroupOwner: Boolean, val groupOwnerAddress: String): CreateGroupEvent()

    data class ShowToast(val message: String): CreateGroupEvent()
}