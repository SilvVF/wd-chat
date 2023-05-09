package io.silv.wifidirectchat.use_case

import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.flow.SharedFlow

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>

fun observeWifiDirectEventsUseCaseImpl(
    receiver: WifiP2pReceiver
) = receiver.eventBroadcast
