package io.silv.feature_search_users

import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_search_users.use_case.ConnectToDeviceUseCase
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.SearchUsersUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

fun log(item: String) {
    Log.d("searchUsersTag", item)
}

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase
): EventViewModel<SearchUsersEvent>() {


    private val mutableDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())

    val users = mutableDevices.map { list ->
        list.mapNotNull { device -> device.deviceName }
    }

    init {
        viewModelScope.launch {
            launch { searchUsers() }
            wifiDirectEventsUseCase().collect { event ->
                when (event) {
                    is WifiP2pEvent.ConnectionChanged -> Unit
                    is WifiP2pEvent.DiscoveryChanged -> Unit
                    is WifiP2pEvent.PeersChanged -> {
                       mutableDevices.emit(event.peers)
                    }
                    is WifiP2pEvent.StateChanged -> {
                        if (event.enabled) {
                            searchUsers()
                        } else {
                            eventChannel.send(SearchUsersEvent.WifiP2pDisabled)
                        }
                    }
                    WifiP2pEvent.ThisDeviceChanged -> {

                    }
                }
            }
        }
    }

    fun connectToUser(deviceName: String) = viewModelScope.launch {
        mutableDevices.value.
            find { it.deviceName == deviceName }?.let { device ->
                connectToDeviceUseCase(device) {
                    setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                    setPassphrase("password") // TODO(ask for password in setup)
                    setNetworkName("Test-network1")
                }.collect {
                    when(it) {
                        is Either.Left -> {
                            if (it.value.groupFormed) {
                                eventChannel.send(
                                    SearchUsersEvent.JoinedGroup(
                                        owner = it.value.isGroupOwner
                                    )
                                )
                            } else {
                                // handle failed ot form group
                            }
                        }
                        is Either.Right -> {
                            //failed to connect
                            eventChannel.send(SearchUsersEvent.ShowToast(it.value.message))
                        }
                    }
                }
        }
    }

    private fun searchUsers() = viewModelScope.launch {
        searchUsersUseCase().collect { it ->
            when(it) {
                is Either.Left -> {
                    mutableDevices.emit(it.value)
                }
                is Either.Right -> {
                    eventChannel.send(
                        SearchUsersEvent.ShowToast(it.value.message)
                    )
                }
            }
        }
    }


}

sealed class SearchUsersEvent {
    object WifiP2pDisabled: SearchUsersEvent()
    data class ShowToast(val text: String): SearchUsersEvent()

    data class JoinedGroup(val owner: Boolean): SearchUsersEvent()
}