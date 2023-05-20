package io.silv.feature_search_users

import android.net.MacAddress
import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_search_users.use_case.ConnectToDeviceUseCase
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.RefreshDeviceListUseCase
import io.silv.feature_search_users.use_case.StartDiscoveryUseCase
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val startDiscovery: StartDiscoveryUseCase,
    private val refreshDeviceListUseCase: RefreshDeviceListUseCase
): EventViewModel<SearchUsersEvent>() {


    private val mutableDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    private val mutableCode = MutableStateFlow("")
    private val mutableNetworkName = MutableStateFlow("")
    private val mutableRefreshFlow = MutableStateFlow(false)


    val refreshing = mutableRefreshFlow
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    data class UiDevice(
        val name: String,
        val mac: String,
    )

    val users = mutableDevices.map { list ->
        list.map { device ->
            UiDevice(
                name = device.deviceName,
                mac = device.deviceAddress,
            )
        }
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
                }.onLeft {error ->
                        eventChannel.send(
                            SearchUsersEvent.ShowSnackbar(error.message)
                        )
                    }
            }
    }

    private fun observeWifiDirectEvents() = viewModelScope.launch {
        wifiDirectEventsUseCase().collect { event ->
            when (event) {
                is WifiP2pEvent.ConnectionChanged -> {
                    if (event.p2pInfo.groupFormed) {
                        eventChannel.send(
                            SearchUsersEvent.JoinedGroup(
                                groupOwnerAddress = event.p2pInfo
                                    .groupOwnerAddress
                                    .toString()
                                    .replace("/", ""),
                                isGroupOwner = event.p2pInfo.isGroupOwner
                            )
                        )
                    }
                }
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
                else -> Unit
            }
        }
    }

    fun onPullToRefresh() = viewModelScope.launch {
        refreshDeviceListUseCase()
            .onRight { deviceList ->
                if (deviceList.isNotEmpty()) {
                    mutableDevices.emit(deviceList)
                }
            }
            .onLeft {
                when (it) {
                    is P2pError.GenericError -> eventChannel.send(
                        SearchUsersEvent.ShowSnackbar(it.message)
                    )
                    is P2pError.MissingPermission -> eventChannel.send(
                        SearchUsersEvent.MissingPermissions
                    )
                }
            }
    }

    private fun startSearchingNearbyDevices() = viewModelScope.launch {
        startDiscovery()
            .onLeft {
                eventChannel.send(
                    SearchUsersEvent.ShowSnackbar(it.message)
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }
}

sealed class SearchUsersEvent {

    object MissingPermissions: SearchUsersEvent()

    object WifiP2pDisabled: SearchUsersEvent()

    data class ShowSnackbar(val text: String): SearchUsersEvent()

    data class JoinedGroup(
        val isGroupOwner: Boolean,
        val groupOwnerAddress : String,
    ): SearchUsersEvent()
}