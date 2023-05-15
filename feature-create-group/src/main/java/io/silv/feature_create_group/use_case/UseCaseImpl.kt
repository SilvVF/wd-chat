package io.silv.feature_create_group.use_case

import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

internal suspend fun createGroupUseCaseImpl(
    p2p: P2p,
    passPhrase: String,
    networkName: String
): Either<P2pError, Boolean> {
    return p2p.createGroup(
        passPhrase,
        networkName
    )
}

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): Flow<WifiP2pEvent> {
    return wifiP2pReceiver.p2pBroadcast
        .flowOn(Dispatchers.IO)
}

