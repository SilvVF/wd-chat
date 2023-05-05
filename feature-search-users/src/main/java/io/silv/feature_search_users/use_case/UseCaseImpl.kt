package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): SharedFlow<WifiP2pEvent> {
    return wifiP2pReceiver.eventBroadcast
}

internal fun startDiscoveryUseCaseImpl(
    p2p: P2p
): Flow<Either<P2pError, Boolean>> = p2p.startDiscovery()

internal fun observePeersListUseCaseImpl(
    p2p: P2p
): Flow<List<WifiP2pDevice>> = p2p.peersFlow

internal fun observeGroupInfoUseCaseImpl(
    p2p: P2p
): Flow<WifiP2pGroup> = p2p.groupInfoFlow


internal fun connectToDeviceUseCaseImpl(
    p2p: P2p,
    wifiP2pDevice: WifiP2pDevice,
    config: WifiP2pConfig.Builder.() -> Unit
): Flow<Either<P2pError, Boolean>> = p2p.connect(wifiP2pDevice, config)