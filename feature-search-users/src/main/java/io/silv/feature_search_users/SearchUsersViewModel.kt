package io.silv.feature_search_users

import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_search_users.use_case.ConnectToDeviceUseCase
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.StartDiscoveryUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val startDiscovery: StartDiscoveryUseCase
): EventViewModel<SearchUsersEvent>() {


    private val mutableDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())

    val users = mutableDevices.map { list ->
        list.mapNotNull { device -> device.deviceName }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        startSearchingNearbyDevices()
        observeWifiDirectEvents()
    }


    fun connectToUser(deviceName: String) = viewModelScope.launch {
        mutableDevices.value.find { it.deviceName == deviceName }
            ?.let { device ->
                connectToDeviceUseCase(device) {
                    setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                    setPassphrase("password") // TODO(ask for password in setup)
                }
                    .onLeft {error ->
                        eventChannel.send(
                            SearchUsersEvent.ShowToast(error.message)
                        )
                    }
            }
    }


    private fun observeWifiDirectEvents() = viewModelScope.launch {
        wifiDirectEventsUseCase().collect { event ->
            when (event) {
                is WifiP2pEvent.ConnectionChanged -> {
                    if (event.p2pInfo.isGroupOwner) {
                        SearchUsersEvent.JoinedGroup(
                            groupOwnerAddress = event.p2pInfo.groupOwnerAddress.toString(),
                            isGroupOwner = event.p2pInfo.isGroupOwner
                        )
                    }
                }
                is WifiP2pEvent.DiscoveryChanged -> Unit
                is WifiP2pEvent.PeersChanged -> {
                    mutableDevices.emit(event.peers)
                }
                is WifiP2pEvent.StateChanged -> {
                    if (event.enabled) {
                        startSearchingNearbyDevices()
                    } else {
                        eventChannel.send(SearchUsersEvent.WifiP2pDisabled)
                    }
                }
                WifiP2pEvent.ThisDeviceChanged -> {
                    // TODO()
                }
            }
        }
    }

    private fun startSearchingNearbyDevices() = viewModelScope.launch {
        startDiscovery()
            .onLeft {
                eventChannel.send(
                    SearchUsersEvent.ShowToast(it.message)
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }
}

sealed class SearchUsersEvent {
    object WifiP2pDisabled: SearchUsersEvent()

    data class ShowToast(val text: String): SearchUsersEvent()

    data class JoinedGroup(
        val isGroupOwner: Boolean,
        val groupOwnerAddress : String,
    ): SearchUsersEvent()
}