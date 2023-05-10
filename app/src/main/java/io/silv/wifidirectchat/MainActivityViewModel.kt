package io.silv.wifidirectchat


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifidirectchat.use_case.ObserveWifiDirectEventsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase
): EventViewModel<MainActivityEvent>() {

    var onboarded by mutableStateOf(false)

    fun collectWifiEvents() = viewModelScope.launch {
            observeWifiDirectEventsUseCase().collect { event ->
                logEvent(event)
                when (event) {
                    is WifiP2pEvent.StateChanged ->  {
                        if (!event.enabled && onboarded) {
                            // TODO (Check Permsissions and show error that wifi direct is disabled)
                        }
                    }
                    else -> Unit
                }
            }
    }

    private fun logEvent(event: WifiP2pEvent) {
        Log.d("WIFI_P2P_EVENT", event.toString())
    }
}

sealed interface MainActivityEvent {
    object WifiP2pDisabled: MainActivityEvent
}