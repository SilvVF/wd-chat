package io.silv.feature_create_group.use_case


import arrow.core.Either
import io.silv.wifi_direct.WifiP2pEvent
import io.silv.wifi_direct.types.P2pError
import kotlinx.coroutines.flow.SharedFlow

data class GroupInfo(
    val networkName: String,
    val passPhrase: String
)

fun interface CreateGroupUseCase: suspend (GroupInfo) -> Either<P2pError, Boolean>

fun interface ObserveWifiDirectEventsUseCase: () -> SharedFlow<WifiP2pEvent>