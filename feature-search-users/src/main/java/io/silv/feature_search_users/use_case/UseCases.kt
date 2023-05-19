package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import arrow.core.Either
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow



fun interface StartDiscoveryUseCase: suspend () -> Either<P2pError, Boolean>

fun interface ObserveWifiDirectEventsUseCase: () -> Flow<WifiP2pEvent>

fun interface ConnectToDeviceUseCase: suspend (
    WifiP2pDevice,
    WifiP2pConfig.Builder.() -> Unit
) -> Either<P2pError, Boolean>

fun interface RefreshDeviceListUseCase: suspend () -> Either<P2pError, List<WifiP2pDevice>>