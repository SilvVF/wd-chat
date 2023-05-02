package io.silv.feature_search_users.use_case

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import arrow.core.Either
import io.silv.wifi_direct.P2p
import io.silv.wifi_direct.P2pError
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.WifiP2pReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow



fun interface SearchUsersUseCase: () -> Flow<Either<List<WifiP2pDevice>, P2pError>>

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>

fun interface ConnectToDeviceUseCase: () -> Flow<Either<WifiP2pInfo, P2pError>>
