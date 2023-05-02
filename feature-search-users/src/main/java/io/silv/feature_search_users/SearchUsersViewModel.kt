package io.silv.feature_search_users

import android.util.Log
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.feature_search_users.use_case.ObserveWifiDirectEventsUseCase
import io.silv.feature_search_users.use_case.SearchUsersUseCase
import io.silv.feature_search_users.use_case.searchUsersUseCaseImpl
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

fun log(item: String) {
    Log.d("searchUsersTag", item)
}

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    wifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase
): EventViewModel<SearchUsersEvent>() {

    private val _users = MutableStateFlow<List<String>>(emptyList())
    val users = _users.asStateFlow()

    init {
        viewModelScope.launch {
            launch { searchUsers() }
            wifiDirectEventsUseCase().collect { event ->
                when (event) {
                    is WifiP2pEvent.ConnectionChanged -> Unit
                    is WifiP2pEvent.DiscoveryChanged -> Unit
                    is WifiP2pEvent.PeersChanged -> {
                       _users.emit(
                           event.peers.map { it.deviceName.toString() }
                       )
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

    private fun searchUsers() = viewModelScope.launch {
        searchUsersUseCase().collect { it ->
            when(it) {
                is Either.Left -> {
                    _users.emit(
                        it.value.map { it.deviceName.toString() }
                    )
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
}