package io.silv.feature_search_users

import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_search_users.use_case.ConnectToDeviceUseCase
import io.silv.feature_search_users.use_case.ObserveGroupInfoUseCase
import io.silv.feature_search_users.use_case.ObservePeersListUseCase
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.StartDiscoveryUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val observePeersList: ObservePeersListUseCase,
    private val observeGroupInfo: ObserveGroupInfoUseCase,
    private val startDiscovery: StartDiscoveryUseCase
): EventViewModel<SearchUsersEvent>() {


    private val mutableDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())

    val users = mutableDevices.map { list ->
        list.mapNotNull { device -> device.deviceName }
    }

    init {
        startSearchingNearbyDevices()
        observePeers()
        observeWifiDirectEvents()
        observeGroup()
    }

    private fun observePeers() = viewModelScope.launch {
        observePeersList().collect { peers ->
            mutableDevices.emit(peers)
        }
    }

    private fun observeGroup() = viewModelScope.launch {
        observeGroupInfo().collect { wifiP2pGroup ->
             // TODO()
        }
    }

    fun connectToUser(deviceName: String) = viewModelScope.launch {
        mutableDevices.value.
            find { it.deviceName == deviceName }
            ?.let { device ->
                connectToDeviceUseCase(device) {
                    setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                    setPassphrase("password") // TODO(ask for password in setup)
                    setNetworkName("DIRECT-xy" + "TestNetwork")
                }.first()
                    .fold(
                        ifLeft = { err : P2pError ->
                            when (err) {
                                is P2pError.GenericError -> {}
                                is P2pError.MissingPermission -> {}
                            }
                        },
                        ifRight = { connected ->
                           // TODO()
                        }
                    )
            }
    }


    private fun observeWifiDirectEvents() = viewModelScope.launch {
        wifiDirectEventsUseCase().collect { event ->
            when (event) {
                is WifiP2pEvent.ConnectionChanged -> Unit
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

    fun startSearchingNearbyDevices() = viewModelScope.launch {
        startDiscovery().first().fold(
                ifLeft = {err ->
                    when (err) {
                        is P2pError.GenericError -> TODO()
                        is P2pError.MissingPermission -> TODO()
                    }
                },
                ifRight = { started ->
                    // TODO()
                }
            )
    }


}

sealed class SearchUsersEvent {
    object WifiP2pDisabled: SearchUsersEvent()
    data class ShowToast(val text: String): SearchUsersEvent()

    data class JoinedGroup(val owner: Boolean): SearchUsersEvent()
}