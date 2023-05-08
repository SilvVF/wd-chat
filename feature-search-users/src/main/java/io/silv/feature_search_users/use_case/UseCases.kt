package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import arrow.core.Either
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow



fun interface StartDiscoveryUseCase: suspend () -> Either<P2pError, Boolean>

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>

fun interface ConnectToDeviceUseCase: suspend (
    WifiP2pDevice,
    WifiP2pConfig.Builder.() -> Unit
) -> Either<P2pError, Boolean>
