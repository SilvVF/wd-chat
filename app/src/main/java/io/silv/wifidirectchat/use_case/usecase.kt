package io.silv.wifidirectchat.use_case

import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOn

fun interface ObserveWifiDirectEventsUseCase: () -> Flow<WifiP2pEvent>

fun observeWifiDirectEventsUseCaseImpl(
    receiver: WifiP2pReceiver
) = receiver.p2pBroadcast
    .flowOn(Dispatchers.IO)
