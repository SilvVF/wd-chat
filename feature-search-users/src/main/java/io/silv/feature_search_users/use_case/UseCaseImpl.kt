package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.P2pError
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

internal fun observeWifiDirectEventsUseCaseImpl(
    wifiP2pReceiver: WifiP2pReceiver
): SharedFlow<WifiP2pEvent> {
    return wifiP2pReceiver.eventBroadcast
}

internal fun searchUsersUseCaseImpl(
    p2p: P2p
): Flow<Either<List<WifiP2pDevice>, P2pError>> = p2p.getNearbyDevices()


internal suspend fun connectToDeviceUseCaseImpl(
    p2p: P2p,
    wifiP2pDevice: WifiP2pDevice,
    config: WifiP2pConfig.Builder.() -> Unit
): Flow<Either<WifiP2pInfo, P2pError>> = p2p.connect(
    device = wifiP2pDevice,
    config = config
)