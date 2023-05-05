package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow



fun interface StartDiscoveryUseCase: () -> Flow<Either<P2pError, Boolean>>
fun interface ObserveGroupInfoUseCase: () -> Flow<WifiP2pGroup>
fun interface ObservePeersListUseCase: () -> Flow<List<WifiP2pDevice>>

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>

fun interface ConnectToDeviceUseCase: (
    WifiP2pDevice,
    WifiP2pConfig.Builder.() -> Unit
) -> Flow<Either<P2pError, Boolean>>
