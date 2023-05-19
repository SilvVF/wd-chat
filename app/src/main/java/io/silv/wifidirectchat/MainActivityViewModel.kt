package io.silv.wifidirectchat


import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.silv.datastore.EncryptedDatastore
import io.silv.shared_ui.utils.EventViewModel
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifidirectchat.use_case.ObserveWifiDirectEventsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val observeWifiDirectEventsUseCase: ObserveWifiDirectEventsUseCase,
    private val datastore: EncryptedDatastore
): EventViewModel<MainActivityEvent>() {

   val usernameFlow = flow {
       datastore.readUserName().collect {
           emit(it ?: "")
       }
   }

   val profilePictureFlow = flow {
       datastore.readProfilePictureUri().collect {
           it?.let { emit(it) }
       }
   }

    val onboarded = flow {
        datastore.readOnboardCompleted().collect {
            emit(it)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun collectWifiEvents() = viewModelScope.launch {
            observeWifiDirectEventsUseCase().collect { event ->
                logEvent(event)
                when (event) {
                    is WifiP2pEvent.StateChanged ->  {
                        if (!event.enabled && onboarded.value == true) {
                            // TODO (Check Permsissions and show error that wifi direct is disabled)
                        }
                    }
                    else -> Unit
                }
            }
    }

    fun onboardComplete() = viewModelScope.launch {
        datastore.writeOnboardCompleted(true)
    }

    private fun logEvent(event: WifiP2pEvent) {
        Log.d("WIFI_P2P_EVENT", event.toString())
    }
}

sealed interface MainActivityEvent {
    object WifiP2pDisabled: MainActivityEvent
}