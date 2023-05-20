package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): Flow<WifiP2pEvent> {
    return wifiP2pReceiver.p2pBroadcast
        .flowOn(Dispatchers.IO)
}

internal suspend fun startDiscoveryUseCaseImpl(
    p2p: P2p
): Either<P2pError, Boolean> = p2p.startDiscovery()



internal suspend fun connectToDeviceUseCaseImpl(
    p2p: P2p,
    wifiP2pDevice: WifiP2pDevice,
    config: WifiP2pConfig.Builder.() -> Unit
): Either<P2pError, Boolean> = runCatching { p2p.connect(wifiP2pDevice, config) }.getOrDefault(Either.Left(P2pError.GenericError("Unknown Error")))

internal suspend fun refreshDeviceListUseCaseImpl(
    p2p: P2p
): Either<P2pError, List<WifiP2pDevice>> {
    return p2p.requestDeviceList()
}